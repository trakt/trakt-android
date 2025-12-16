package tv.trakt.trakt.core.summary.movies.features.streaming.usecases

import android.icu.util.Currency
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.PopularStreamingServices
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingSource
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.common.model.streamings.StreamingType.FREE
import tv.trakt.trakt.common.model.streamings.StreamingType.PURCHASE
import tv.trakt.trakt.common.model.streamings.StreamingType.RENT
import tv.trakt.trakt.common.model.streamings.StreamingType.SUBSCRIPTION
import tv.trakt.trakt.common.model.streamings.fromDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.StreamingServiceDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.streamings.model.StreamingsResult

internal class GetMovieStreamingsUseCase(
    private val remoteMovieSource: MoviesRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
) {
    suspend fun getStreamings(
        user: User,
        movieId: TraktId,
    ): StreamingsResult {
        return coroutineScope {
            if (!localStreamingSource.isValid()) {
                val sources = remoteStreamingSource
                    .getStreamingSources()
                    .asyncMap { StreamingSource.fromDto(it) }

                localStreamingSource.upsertStreamingSources(sources)
            }

            val countryCode = user.streamings?.country ?: DEFAULT_COUNTRY_CODE
            val sources = localStreamingSource.getAllStreamingSources()

            val streamingsAsync = async {
                remoteMovieSource.getStreamings(
                    movieId = movieId,
                    countryCode = countryCode,
                )
            }
            val justWatchLinkAsync = async {
                try {
                    remoteMovieSource.getJustWatchLink(
                        movieId = movieId,
                        countryCode = countryCode,
                    )
                } catch (error: Exception) {
                    error.rethrowCancellation {
                        Timber.e(error)
                    }
                    null
                }
            }

            val streamings = streamingsAsync.await()
            val justWatchLink = justWatchLinkAsync.await()

            val streamingsGroups = groupStreamings(
                streamings = streamings,
                sources = sources,
            )

            val streamingsResult = streamingsGroups
                .flatMap { entry ->
                    entry.value.map { service ->
                        service to entry.key
                    }
                }
                .toImmutableList()

            val ranks = streamings[countryCode]?.streamingRanks
            val ranksResult = StreamingsResult.Ranks(
                rank = ranks?.rank,
                delta = ranks?.delta,
                link = ranks?.link,
            )

            StreamingsResult(
                streamings = streamingsResult,
                ranks = ranksResult,
                justWatchLink = justWatchLink,
            )
        }
    }

    private fun groupStreamings(
        streamings: Map<String, StreamingDto>,
        sources: Map<String, StreamingSource>,
    ): Map<StreamingType, List<StreamingService>> {
        val resultMap = mapOf<StreamingType, MutableList<StreamingService>>(
            SUBSCRIPTION to mutableListOf(),
            PURCHASE to mutableListOf(),
            RENT to mutableListOf(),
            FREE to mutableListOf(),
        )

        streamings.forEach { (country, streaming) ->
            val subscriptions = resultMap.getValue(SUBSCRIPTION)
            val free = resultMap.getValue(FREE)
            val purchase = resultMap.getValue(PURCHASE)
            val rent = resultMap.getValue(RENT)

            subscriptions.addAll(
                streaming.subscription.mapNotNull { subscription ->
                    val source = sources[subscription.source]
                    if (source == null || subscription.linkDirect.isNullOrBlank()) {
                        return@mapNotNull null
                    }
                    createService(
                        country = country,
                        source = source,
                        service = subscription,
                    )
                },
            )

            free.addAll(
                streaming.free.mapNotNull { free ->
                    val source = sources[free.source]
                    if (source == null || free.linkDirect.isNullOrBlank()) {
                        return@mapNotNull null
                    }

                    createService(
                        country = country,
                        source = source,
                        service = free,
                    )
                },
            )

            streaming.purchase.forEach {
                val source = sources[it.source]
                if (source == null || it.linkDirect.isNullOrBlank()) {
                    return@forEach
                }

                val service = createService(
                    country = country,
                    source = source,
                    service = it,
                )

                if (!it.prices.purchase.isNullOrBlank()) {
                    purchase.add(service)
                }
                if (!it.prices.rent.isNullOrBlank()) {
                    rent.add(service)
                }
            }
        }

        val priorityServices = PopularStreamingServices.reversed()

        val servicesComparator = compareByDescending<StreamingService> {
            priorityServices.indexOf(it.source)
        }.thenBy {
            it.source
        }

        return resultMap.mapValues { entry ->
            entry.value
                .sortedWith(servicesComparator)
        }
    }

    private fun createService(
        country: String,
        source: StreamingSource,
        service: StreamingServiceDto,
    ): StreamingService {
        return StreamingService(
            name = source.name,
            linkDirect = service.linkDirect,
            source = service.source,
            color = source.color,
            logo = source.images.logo,
            channel = source.images.channel,
            uhd = service.uhd,
            country = country,
            purchasePrice = service.prices.purchase,
            rentPrice = service.prices.rent,
            currency = service.currency?.let { Currency.getInstance(it) },
        )
    }
}
