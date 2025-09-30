package tv.trakt.trakt.core.summary.movies.usecases

import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieDetailsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getLocalMovie(movieId: TraktId): Movie? {
        return localSource.getMovie(movieId)
    }

    suspend fun getMovie(movieId: TraktId): Movie? {
        return remoteSource.getDetails(movieId)
            .let { Movie.fromDto(it) }
            .also {
                localSource.upsertMovies(listOf(it))
            }
    }
}
