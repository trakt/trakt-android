package tv.trakt.trakt.app.core.streamings.usecase

import android.icu.util.Currency
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.app.core.streamings.model.StreamingServiceRow
import tv.trakt.trakt.app.core.streamings.model.StreamingSource
import tv.trakt.trakt.app.core.streamings.model.StreamingType
import tv.trakt.trakt.app.core.streamings.model.StreamingType.FAVORITE
import tv.trakt.trakt.app.core.streamings.model.StreamingType.FREE
import tv.trakt.trakt.app.core.streamings.model.StreamingType.PURCHASE
import tv.trakt.trakt.app.core.streamings.model.StreamingType.RENT
import tv.trakt.trakt.app.core.streamings.model.StreamingType.SUBSCRIPTION
import tv.trakt.trakt.app.core.streamings.model.fromDto
import tv.trakt.trakt.common.Config.DEFAULT_COUNTRY_CODE
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.StreamingServiceDto

private val popularCountries = mutableListOf(
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

private val popularServices = setOf(
    "netflix",
    "netflix_standard_with_ads",
    "apple_tv_plus",
    "apple_tv",
    "disney_plus",
    "amazon_prime_video",
    "amazon_prime_video_free_with_ads",
    "hbo_max",
    "hbo_max_amazon_channel",
    "hulu",
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
        mediaType: String,
        seasonEpisode: SeasonEpisode?,
    ): ImmutableMap<StreamingType, List<StreamingServiceRow>> {
        require(mediaType in arrayOf("show", "movie", "episode")) {
            "Invalid media type: $mediaType"
        }

        if (!localStreamingSource.isValid()) {
            val sources = remoteStreamingSource
                .getStreamingSources()
                .asyncMap { StreamingSource.fromDto(it) }

            localStreamingSource.upsertStreamingSources(sources)
        }

        val userCountry = user.streamings?.country ?: DEFAULT_COUNTRY_CODE
        popularCountries.add(0, userCountry)

        // Ex. input favorite source: "pl-hbo_max", "us-netflix"
        val favoriteSources = user.streamings?.favorites
            ?.map { it.substringAfter("-") }
            ?.toSet().orEmpty()

        val sources = localStreamingSource
            .getAllStreamingSources()

        val streamings = when (mediaType) {
            "show" -> remoteShowSource.getShowStreamings(
                showId = mediaId,
                countryCode = null,
            )
            "movie" -> remoteMovieSource.getMovieStreamings(
                movieId = mediaId,
                countryCode = null,
            )
            "episode" -> remoteEpisodeSource.getEpisodeStreamings(
                showId = mediaId,
                season = seasonEpisode?.season ?: 0,
                episode = seasonEpisode?.episode ?: 0,
                countryCode = null,
            )
            else -> throw IllegalArgumentException("Unsupported media type: $mediaType")
        }

        return groupStreamings(
            streamings = streamings,
            sources = sources,
            favoriteSources = favoriteSources,
            userCountry = userCountry,
        ).toImmutableMap()
    }

    private fun groupStreamings(
        streamings: Map<String, StreamingDto>,
        sources: Map<String, StreamingSource>,
        favoriteSources: Set<String>,
        userCountry: String,
    ): Map<StreamingType, List<StreamingServiceRow>> {
        val resultMap = mapOf<StreamingType, MutableList<StreamingService>>(
            FAVORITE to mutableListOf(),
            SUBSCRIPTION to mutableListOf(),
            PURCHASE to mutableListOf(),
            RENT to mutableListOf(),
            FREE to mutableListOf(),
        )

        streamings.forEach { (country, streaming) ->
            val favorite = resultMap.getValue(FAVORITE)
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
                    ).also {
                        if (source.source in favoriteSources) {
                            favorite.add(it)
                        }
                    }
                },
            )

            free.addAll(
                streaming.free.mapNotNull { free ->
                    val source = sources[free.source]
                    if (source == null || free.linkDirect.isNullOrBlank()) {
                        return@mapNotNull null
                    }

                    // For free, we only add services if they are available in the user's country
                    if (country != userCountry) {
                        return@forEach
                    }

                    createService(
                        country = country,
                        source = source,
                        service = free,
                    ).also {
                        if (source.source in favoriteSources) {
                            favorite.add(it)
                        }
                    }
                },
            )

            streaming.purchase.forEach {
                val source = sources[it.source]
                if (source == null || it.linkDirect.isNullOrBlank()) {
                    return@forEach
                }

                // For purchase and rent, we only add services if they are available in the user's country
                if (country != userCountry) {
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

        val priorityCountries = popularCountries.toSet().reversed()
        val priorityServices = popularServices.reversed()

        val servicesComparator = compareByDescending<StreamingService> {
            priorityServices.indexOf(it.source)
        }.thenBy {
            it.source
        }

        val rowComparator = compareByDescending<StreamingService> {
            priorityCountries.indexOf(it.country.lowercase())
        }.thenBy {
            it.country
        }

        return resultMap.mapValues { entry ->
            entry.value
                .sortedWith(servicesComparator)
                .groupBy { service -> service.source }
                .map { (source, rowServices) ->
                    StreamingServiceRow(
                        source = source,
                        services = rowServices
                            .distinctBy { it.country }
                            .sortedWith(rowComparator),
                    )
                }
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
