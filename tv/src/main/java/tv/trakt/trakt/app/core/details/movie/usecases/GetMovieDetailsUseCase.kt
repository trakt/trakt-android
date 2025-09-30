package tv.trakt.trakt.app.core.details.movie.usecases

import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

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
