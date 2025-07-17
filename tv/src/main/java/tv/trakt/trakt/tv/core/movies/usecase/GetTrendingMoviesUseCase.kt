package tv.trakt.trakt.tv.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.tv.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.tv.core.movies.model.Movie
import tv.trakt.trakt.tv.core.movies.model.TrendingMovie
import tv.trakt.trakt.tv.core.movies.model.fromDto
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetTrendingMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getTrendingMovies(): ImmutableList<TrendingMovie> {
        return remoteSource.getTrendingMovies()
            .asyncMap {
                TrendingMovie(
                    watchers = it.watchers,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                localSource.upsertMovies(movies.map { it.movie })
            }
    }
}
