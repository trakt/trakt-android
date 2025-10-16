package tv.trakt.trakt.core.summary.shows.features.streaming.usecases

import android.icu.util.Currency
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.PopularStreamingServices
import tv.trakt.trakt.common.helpers.extensions.asyncMap
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
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowStreamingsUseCase(
    private val remoteShowSource: ShowsRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
) {
    suspend fun getStreamings(
        user: User,
        showId: TraktId,
    ): ImmutableList<Pair<StreamingService, StreamingType>> {
        if (!localStreamingSource.isValid()) {
            val sources = remoteStreamingSource
                .getStreamingSources()
                .asyncMap { StreamingSource.fromDto(it) }

            localStreamingSource.upsertStreamingSources(sources)
        }

        val sources = localStreamingSource
            .getAllStreamingSources()

        val streamings = remoteShowSource.getStreamings(
            showId = showId,
            countryCode = user.streamings?.country ?: DEFAULT_COUNTRY_CODE,
        )

        val streamingsGroups = groupStreamings(
            streamings = streamings,
            sources = sources,
        )

        return streamingsGroups
            .flatMap { entry ->
                entry.value.map { service ->
                    service to entry.key
                }
            }
            .toImmutableList()
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
            linkAndroid = service.linkAndroid,
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
