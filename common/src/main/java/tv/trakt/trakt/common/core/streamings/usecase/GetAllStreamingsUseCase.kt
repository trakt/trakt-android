package tv.trakt.trakt.common.core.streamings.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.local.cacheSourcesIfNeeded
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.popularCountriesComparator
import tv.trakt.trakt.common.core.streamings.helpers.popularServicesComparator
import tv.trakt.trakt.common.core.streamings.helpers.toStreamingService
import tv.trakt.trakt.common.core.streamings.model.AllStreamingsSection
import tv.trakt.trakt.common.core.streamings.model.StreamingServiceRow
import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingSource
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.common.model.streamings.StreamingType.FAVORITE
import tv.trakt.trakt.common.model.streamings.StreamingType.FREE
import tv.trakt.trakt.common.model.streamings.StreamingType.PURCHASE
import tv.trakt.trakt.common.model.streamings.StreamingType.RENT
import tv.trakt.trakt.common.model.streamings.StreamingType.SUBSCRIPTION
import tv.trakt.trakt.common.networking.StreamingDto

/**
 * Every way to watch a media in every country it streams in, grouped by streaming type and
 * then by source. Sections arrive ordered by [StreamingType.order] with empty ones dropped.
 */
class GetAllStreamingsUseCase(
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
) {
    suspend fun getStreamings(
        user: User,
        request: StreamingsRequest,
    ): ImmutableList<AllStreamingsSection> {
        localStreamingSource.cacheSourcesIfNeeded(remoteStreamingSource)

        val userCountry = user.streamings?.country ?: DEFAULT_COUNTRY_CODE

        // Ex. input favorite source: "pl-hbo_max", "us-netflix"
        val favoriteSources = user.streamings?.favorites
            ?.map { it.substringAfter("-") }
            ?.toSet()
            .orEmpty()

        val sources = localStreamingSource.getAllStreamingSources()
        val streamings = remoteStreamingSource.getStreamings(
            request.copy(countryCode = null),
        )

        return groupStreamings(
            streamings = streamings,
            sources = sources,
            favoriteSources = favoriteSources,
            userCountry = userCountry,
        )
    }

    private fun groupStreamings(
        streamings: Map<String, StreamingDto>,
        sources: Map<String, StreamingSource>,
        favoriteSources: Set<String>,
        userCountry: String,
    ): ImmutableList<AllStreamingsSection> {
        val resultMap = mapOf(
            FAVORITE to mutableListOf<StreamingService>(),
            SUBSCRIPTION to mutableListOf(),
            PURCHASE to mutableListOf(),
            RENT to mutableListOf(),
            FREE to mutableListOf(),
        )

        val favorite = resultMap.getValue(FAVORITE)
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
                    ).also {
                        if (source.source in favoriteSources) {
                            favorite.add(it)
                        }
                    }
                },
            )

            // Free, purchase and rent are only offered for the user's own country.
            if (country != userCountry) {
                return@forEach
            }

            free.addAll(
                streaming.free.mapNotNull { freeService ->
                    val source = sources[freeService.source] ?: return@mapNotNull null
                    if (freeService.linkDirect.isNullOrBlank()) {
                        return@mapNotNull null
                    }

                    freeService.toStreamingService(
                        country = country,
                        source = source,
                    ).also {
                        if (source.source in favoriteSources) {
                            favorite.add(it)
                        }
                    }
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
                if (it.source in favoriteSources) {
                    favorite.add(service)
                }
            }
        }

        val servicesComparator = popularServicesComparator()
        val countriesComparator = popularCountriesComparator(userCountry)

        return StreamingType.entries
            .sortedBy { it.order }
            .mapNotNull { type ->
                val rows = resultMap
                    .getValue(type)
                    .sortedWith(servicesComparator)
                    .groupBy { it.source }
                    .map { (source, rowServices) ->
                        StreamingServiceRow(
                            source = source,
                            services = rowServices
                                .distinctBy { it.country }
                                .sortedWith(countriesComparator)
                                .toImmutableList(),
                        )
                    }

                when {
                    rows.isEmpty() -> null
                    else -> AllStreamingsSection(
                        type = type,
                        rows = rows.toImmutableList(),
                    )
                }
            }
            .toImmutableList()
    }
}
