package tv.trakt.app.tv.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.app.tv.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.movies.model.fromDto
import tv.trakt.app.tv.helpers.extensions.asyncMap

internal class GetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getPopularMovies(): ImmutableList<Movie> {
        return remoteSource.getPopularMovies()
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
                localSource.upsertMovies(movies)
            }
    }
}
