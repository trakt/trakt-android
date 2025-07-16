package tv.trakt.app.tv.core.details.movie.usecases

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.app.tv.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.movies.model.fromDto

internal class GetMovieDetailsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getMovieDetails(movieId: TraktId): Movie? {
        val localMovie = localSource.getMovie(movieId)
        if (localMovie != null) {
            return localMovie
        }

        return remoteSource.getMovieDetails(movieId)
            .let { Movie.fromDto(it) }
            .also { localSource.upsertMovies(listOf(it)) }
    }
}
