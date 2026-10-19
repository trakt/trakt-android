package tv.trakt.trakt.core.home.sections.recommended.usecase.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.recommended.RecommendedItem
import tv.trakt.trakt.common.model.recommended.RecommendedSource
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.recommended.data.movies.RecommendedMoviesLocalSource
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedMoviesUseCase

internal class DefaultGetRecommendedMoviesUseCase(
    private val remoteSource: MoviesRemoteSource,
    private val localRecommendedSource: RecommendedMoviesLocalSource,
    private val localMovieSource: MovieLocalSource,
) : GetRecommendedMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<RecommendedItem.MovieItem> {
        return localRecommendedSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(
                    it.asyncMap { item -> item.movie },
                )
            }
    }

    override suspend fun getMovies(
        limit: Int,
        skipLocal: Boolean,
        filters: GlobalFilter,
    ): ImmutableList<RecommendedItem.MovieItem> {
        return remoteSource.getRecommended(limit = limit, filters = filters)
            .asyncMap { entry ->
                RecommendedItem.MovieItem(
                    movie = Movie.fromDto(entry.movie),
                    sources = entry.sources.orEmpty()
                        .map { RecommendedSource.fromDto(it) }
                        .toImmutableList(),
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localRecommendedSource.setMovies(
                        movies = movies.take(DEFAULT_SECTION_LIMIT),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
