package tv.trakt.trakt.core.streamings.usecase

import android.icu.util.Currency
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.PopularStreamingServices
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingSource
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.common.model.streamings.StreamingType.FAVORITE
import tv.trakt.trakt.common.model.streamings.StreamingType.FREE
import tv.trakt.trakt.common.model.streamings.StreamingType.PURCHASE
import tv.trakt.trakt.common.model.streamings.StreamingType.RENT
import tv.trakt.trakt.common.model.streamings.StreamingType.SUBSCRIPTION
import tv.trakt.trakt.common.model.streamings.fromDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.StreamingServiceDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.streamings.model.AllStreamingsSection
import tv.trakt.trakt.core.streamings.model.StreamingServiceRow

/**
 * Countries surfaced first within a source row, after the user's own streaming country.
 */
private val PopularCountries = listOf(
    "us",
    "gb",
    "ca",
    "de",
    "fr",
    "jp",
    "au",
    "nl",
    "mx",
    "sg",
)

internal class GetAllStreamingsUseCase(
    private val remoteShowSource: ShowsRemoteDataSource,
    private val remoteMovieSource: MoviesRemoteDataSource,
    private val remoteEpisodeSource: EpisodesRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
    private val localStreamingSource: StreamingLocalDataSource,
) {
    suspend fun getStreamings(
        user: User,
        mediaId: TraktId,
        mediaType: MediaType,
        seasonEpisode: SeasonEpisode?,
    ): ImmutableList<AllStreamingsSection> {
        if (!localStreamingSource.isValid()) {
            val sources = remoteStreamingSource
                .getStreamingSources()
                .asyncMap { StreamingSource.fromDto(it) }

            localStreamingSource.upsertStreamingSources(sources)
        }

        val userCountry = user.streamings?.country ?: DEFAULT_COUNTRY_CODE

        // Ex. input favorite source: "pl-hbo_max", "us-netflix"
        val favoriteSources = user.streamings?.favorites
            ?.map { it.substringAfter("-") }
            ?.toSet()
            .orEmpty()

        val sources = localStreamingSource.getAllStreamingSources()

        val streamings = when (mediaType) {
            MediaType.Show -> remoteShowSource.getStreamings(
                showId = mediaId,
                countryCode = null,
            )

            MediaType.Movie -> remoteMovieSource.getStreamings(
                movieId = mediaId,
                countryCode = null,
            )

            MediaType.Episode -> remoteEpisodeSource.getStreamings(
                showId = mediaId,
                season = seasonEpisode?.season ?: 0,
                episode = seasonEpisode?.episode ?: 0,
                countryCode = null,
            )

            MediaType.Season -> error("Unsupported media type: $mediaType")
        }

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

                    createService(
                        country = country,
                        source = source,
                        service = subscription,
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

                    createService(
                        country = country,
                        source = source,
                        service = freeService,
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
                if (it.source in favoriteSources) {
                    favorite.add(service)
                }
            }
        }

        val priorityCountries = (listOf(userCountry) + PopularCountries).reversed()
        val priorityServices = PopularStreamingServices.reversed()

        val servicesComparator = compareByDescending<StreamingService> {
            priorityServices.indexOf(it.source)
        }.thenBy {
            it.source
        }

        val countriesComparator = compareByDescending<StreamingService> {
            priorityCountries.indexOf(it.country.lowercase())
        }.thenBy {
            it.country
        }

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
