package tv.trakt.trakt.common.core.streamings.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.local.cacheSourcesIfNeeded
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.popularServicesComparator
import tv.trakt.trakt.common.core.streamings.helpers.toStreamingService
import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.core.streamings.model.StreamingsResult
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingSource
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.common.model.streamings.StreamingType.FREE
import tv.trakt.trakt.common.model.streamings.StreamingType.PURCHASE
import tv.trakt.trakt.common.model.streamings.StreamingType.RENT
import tv.trakt.trakt.common.model.streamings.StreamingType.SUBSCRIPTION
import tv.trakt.trakt.common.networking.StreamingDto

/**
 * Every way to watch a media in the user's own country, for the details
 * "Where to Watch" row.
 */
class GetStreamingsUseCase(
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
) {
    suspend fun getStreamings(
        user: User,
        request: StreamingsRequest,
    ): StreamingsResult =
        coroutineScope {
            localStreamingSource.cacheSourcesIfNeeded(remoteStreamingSource)

            val countryCode = user.streamings?.country ?: DEFAULT_COUNTRY_CODE
            val countryRequest = request.copy(countryCode = countryCode)
            val sources = localStreamingSource.getAllStreamingSources()

            val streamingsAsync = async {
                remoteStreamingSource.getStreamings(countryRequest)
            }
            val justWatchLinkAsync = async {
                try {
                    remoteStreamingSource.getJustWatchLink(countryRequest)
                } catch (error: Exception) {
                    error.rethrowCancellation {
                        Timber.e(error)
                    }
                    null
                }
            }

            val streamings = streamingsAsync.await()
            val justWatchLink = justWatchLinkAsync.await()

            val ranks = streamings[countryCode]?.streamingRanks

            StreamingsResult(
                streamings = groupStreamings(
                    streamings = streamings,
                    sources = sources,
                ),
                ranks = StreamingsResult.Ranks(
                    rank = ranks?.rank,
                    delta = ranks?.delta,
                    link = ranks?.link,
                ),
                justWatchLink = justWatchLink,
            )
        }

    private fun groupStreamings(
        streamings: Map<String, StreamingDto>,
        sources: Map<String, StreamingSource>,
    ): ImmutableList<Pair<StreamingService, StreamingType>> {
        val resultMap = mapOf(
            SUBSCRIPTION to mutableListOf<StreamingService>(),
            PURCHASE to mutableListOf(),
            RENT to mutableListOf(),
            FREE to mutableListOf(),
        )

        val subscriptions = resultMap.getValue(SUBSCRIPTION)
        val free = resultMap.getValue(FREE)
        val purchase = resultMap.getValue(PURCHASE)
        val rent = resultMap.getValue(RENT)

        streamings.forEach { (country, streaming) ->
            subscriptions.addAll(
                streaming.subscription.mapNotNull { subscription ->
                    val source = sources[subscription.source] ?: return@mapNotNull null
                    if (subscription.linkDirect.isNullOrBlank()) {
                        return@mapNotNull null
                    }

                    subscription.toStreamingService(
                        country = country,
                        source = source,
                    )
                },
            )

            free.addAll(
                streaming.free.mapNotNull { freeService ->
                    val source = sources[freeService.source] ?: return@mapNotNull null
                    if (freeService.linkDirect.isNullOrBlank()) {
                        return@mapNotNull null
                    }

                    freeService.toStreamingService(
                        country = country,
                        source = source,
                    )
                },
            )

            streaming.purchase.forEach {
                val source = sources[it.source] ?: return@forEach
                if (it.linkDirect.isNullOrBlank()) {
                    return@forEach
                }

                val service = it.toStreamingService(
                    country = country,
                    source = source,
                )

                if (!it.prices.purchase.isNullOrBlank()) {
                    purchase.add(service)
                }
                if (!it.prices.rent.isNullOrBlank()) {
                    rent.add(service)
                }
            }
        }

        val servicesComparator = popularServicesComparator()

        return resultMap
            .flatMap { (type, services) ->
                services
                    .sortedWith(servicesComparator)
                    .map { service -> service to type }
            }
            .toImmutableList()
    }
}
