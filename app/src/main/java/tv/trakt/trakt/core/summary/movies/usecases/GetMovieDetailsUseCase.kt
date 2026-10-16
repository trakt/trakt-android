package tv.trakt.trakt.core.summary.movies.usecases

import tv.trakt.trakt.common.core.media.movies.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.movies.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetMovieDetailsUseCase(
    private val remoteSource: MoviesRemoteSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getLocalMovie(movieId: TraktId): Movie? {
        return localSource.getMovie(movieId)
    }

    suspend fun getMovie(movieId: TraktId): Movie {
        return remoteSource.getDetails(movieId)
            .let { Movie.fromDto(it) }
            .also {
                localSource.upsertMovies(listOf(it))
            }
    }

    suspend fun getMovie(movieSlug: SlugId): Movie {
        return remoteSource.getDetails(movieSlug)
            .let { Movie.fromDto(it) }
            .also {
                localSource.upsertMovies(listOf(it))
            }
    }
}
