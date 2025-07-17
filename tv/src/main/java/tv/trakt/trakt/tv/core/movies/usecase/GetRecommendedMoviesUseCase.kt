package tv.trakt.trakt.tv.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.tv.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.tv.core.movies.model.Movie
import tv.trakt.trakt.tv.core.movies.model.fromDto
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetRecommendedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getRecommendedMovies(): ImmutableList<Movie> {
        return remoteSource.getRecommendedMovies()
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.upsertMovies(it)
            }
    }
}
