package tv.trakt.trakt.core.home.sections.recommended.usecase.media

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.MediaRemoteSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.recommended.RecommendedItem
import tv.trakt.trakt.common.model.recommended.RecommendedSource
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaRecommendationResponse
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.recommended.data.media.RecommendedMediaLocalSource

/**
 * Loads interleaved show and movie recommendations for Media mode.
 */
internal class GetRecommendedMediaUseCase(
    private val remoteSource: MediaRemoteSource,
    private val localRecommendedSource: RecommendedMediaLocalSource,
    private val localShowSource: ShowLocalSource,
    private val localMovieSource: MovieLocalSource,
) {
    suspend fun getLocalMedia(): ImmutableList<RecommendedItem> {
        return localRecommendedSource.getItems()
            .toImmutableList()
            .also { upsertMedia(it) }
    }

    suspend fun getMedia(
        limit: Int = DEFAULT_SECTION_LIMIT,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<RecommendedItem> {
        val items = remoteSource.getRecommended(limit = limit, filters = filters)
            .mapNotNull(::toRecommendedItem)
            .toImmutableList()

        if (!skipLocal) {
            localRecommendedSource.setItems(items.take(DEFAULT_SECTION_LIMIT))
        }
        upsertMedia(items)

        return items
    }

    private suspend fun upsertMedia(items: List<RecommendedItem>) {
        localShowSource.upsertShows(
            items.filterIsInstance<RecommendedItem.ShowItem>().map { it.show },
        )
        localMovieSource.upsertMovies(
            items.filterIsInstance<RecommendedItem.MovieItem>().map { it.movie },
        )
    }
}

private fun toRecommendedItem(dto: V3MediaRecommendationResponse): RecommendedItem? {
    val sources = dto.sources.orEmpty()
        .map(RecommendedSource::fromDto)
        .toImmutableList()

    return dto.show?.let { RecommendedItem.ShowItem(show = Show.fromDto(it), sources = sources) }
        ?: dto.movie?.let { RecommendedItem.MovieItem(movie = Movie.fromDto(it), sources = sources) }
}
