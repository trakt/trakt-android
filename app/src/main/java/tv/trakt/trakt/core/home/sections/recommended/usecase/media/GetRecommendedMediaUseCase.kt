package tv.trakt.trakt.core.home.sections.recommended.usecase.media

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.media.data.remote.MediaRemoteDataSource
import tv.trakt.trakt.common.core.media.movies.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.media.shows.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaRecommendationResponse
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.recommended.data.media.RecommendedMediaLocalDataSource
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedSource

/**
 * Loads interleaved show and movie recommendations for Media mode.
 */
internal class GetRecommendedMediaUseCase(
    private val remoteSource: MediaRemoteDataSource,
    private val localRecommendedSource: RecommendedMediaLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
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
