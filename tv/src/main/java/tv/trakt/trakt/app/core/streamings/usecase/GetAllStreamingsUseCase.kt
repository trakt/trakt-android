package tv.trakt.trakt.app.core.streamings.usecase

import android.icu.util.Currency
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import org.openapitools.client.models.GetMoviesWatchnow200ResponseValueCableInner
import tv.trakt.trakt.app.common.model.SeasonEpisode
import tv.trakt.trakt.app.common.model.StreamingService
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.app.core.streamings.model.StreamingSource
import tv.trakt.trakt.app.core.streamings.model.fromDto
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.networking.StreamingDto

internal class GetAllStreamingsUseCase(
    private val remoteShowSource: ShowsRemoteDataSource,
    private val remoteMovieSource: MoviesRemoteDataSource,
    private val remoteEpisodeSource: EpisodesRemoteDataSource,
    private val remoteStreamingSource: StreamingRemoteDataSource,
) {
    suspend fun getStreamings(
        user: User,
        mediaId: TraktId,
        mediaType: String,
        seasonEpisode: SeasonEpisode?,
    ): ImmutableMap<String, List<StreamingService>> {
        require(mediaType in arrayOf("show", "movie", "episode")) {
            "Invalid media type: $mediaType"
        }

        val favoriteSources = user.streamings?.favorites?.toSet().orEmpty()
        val sources = remoteStreamingSource
            .getStreamingSources()
            .associateBy(
                keySelector = { it.source },
                valueTransform = { StreamingSource.fromDto(it) },
            )

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

        val groupedStreamings = groupStreamings(
            streamings = streamings,
            sources = sources,
            favoriteSources = favoriteSources,
        )

        return groupedStreamings.toImmutableMap()
    }

    private fun groupStreamings(
        streamings: Map<String, StreamingDto>,
        sources: Map<String, StreamingSource>,
        favoriteSources: Set<String>,
    ): Map<String, List<StreamingService>> {
        fun createService(
            country: String,
            source: StreamingSource,
            option: GetMoviesWatchnow200ResponseValueCableInner,
        ) = StreamingService(
            name = source.name,
            linkDirect = option.linkDirect,
            source = option.source,
            color = source.color,
            logo = source.images.logo,
            uhd = option.uhd,
            country = country,
            purchasePrice = option.prices.purchase,
            rentPrice = option.prices.rent,
            currency = option.currency?.let { Currency.getInstance(it) },
        )

        return buildMap {
            val favorite = mutableListOf<StreamingService>()
            val subscription = mutableListOf<StreamingService>()
            val free = mutableListOf<StreamingService>()
            val purchase = mutableListOf<StreamingService>()
            val rent = mutableListOf<StreamingService>()

            streamings.forEach { (country, streaming) ->
                subscription.addAll(
                    streaming.subscription.mapNotNull { subscription ->
                        val source = sources[subscription.source]
                        if (source == null || subscription.linkDirect.isNullOrBlank()) {
                            return@mapNotNull null
                        }
                        createService(
                            country = country,
                            source = source,
                            option = subscription,
                        ).also {
                            val key = "$country-${it.source}"
                            if (favoriteSources.any { fav -> fav == key }) {
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
                        createService(
                            country = country,
                            source = source,
                            option = free,
                        ).also {
                            val key = "$country-${it.source}"
                            if (favoriteSources.any { fav -> fav == key }) {
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

                    val service = createService(
                        country = country,
                        source = source,
                        option = it,
                    )

                    if (!it.prices.purchase.isNullOrBlank()) {
                        purchase.add(service)
                    }
                    if (!it.prices.rent.isNullOrBlank()) {
                        rent.add(service)
                    }
                    if (favoriteSources.any { fav -> fav == "$country-${it.source}" }) {
                        favorite.add(service)
                    }
                }

                put(
                    "favorite",
                    favorite
                        .distinctBy { it.source }
                        .sortedWith(compareBy({ it.source }, { it.country })),
                )

                with(
                    compareBy<StreamingService>(
                        { !popularServices.contains(it.source) },
                        { it.source },
                        { it.country },
                    ),
                ) {
                    put("subscription", subscription.sortedWith(this))
                    put("free", free.sortedWith(this))
                    put("purchase", purchase.sortedWith(this))
                    put("rent", rent.sortedWith(this))
                }
            }
        }
    }
}

private val popularServices = setOf(
    "netflix",
    "netflix_standard_with_ads",
    "hbo_max",
    "hulu",
    "amazon_video",
    "amazon_prime",
    "amazon_prime_video",
    "apple_tv",
    "apple_tv_plus",
    "disney_plus",
    "peacock",
    "paramount_plus",
    "crunchyroll",
)
