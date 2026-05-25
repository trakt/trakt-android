package tv.trakt.trakt.core.discover.sections.popular.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class DefaultGetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localPopularSource: PopularMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) : GetPopularMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<DiscoverItem.MovieItem> {
        return localPopularSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(
                    it.asyncMap { item -> item.movie },
                )
            }
    }

    override suspend fun getMovies(
        limit: Int,
        page: Int,
        skipLocal: Boolean,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem.MovieItem> {
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            filters = filters,
        )
            .mapIndexed { index, movieDto ->
                DiscoverItem.MovieItem(
                    movie = Movie.fromDto(movieDto),
                    count = index + 1,
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localPopularSource.setMovies(
                        movies = movies.take(DiscoverConfig.DEFAULT_SECTION_LIMIT),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
