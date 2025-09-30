package tv.trakt.trakt.app.core.details.movie.usecases.streamings

import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetPlexUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
    private val remoteMovieSource: MoviesRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getPlexStatus(movieId: TraktId): Result {
        var movie = localMovieSource.getMovie(movieId)

        // If we don't have the movie or it doesn't have a Plex ID, fetch details from remote and update.
        if (movie == null || movie.ids.plex == null) {
            movie = remoteMovieSource.getMovieDetails(movieId)
                .let { Movie.fromDto(it) }
                .also { localMovieSource.upsertMovies(listOf(it)) }
        }

        val result = remoteSyncSource.getPlexCollection()
        return Result(
            isPlex = result.containsKey(movieId) && movie.ids.plex != null,
            plexSlug = movie.ids.plex,
        )
    }

    data class Result(
        val isPlex: Boolean,
        val plexSlug: SlugId?,
    )
}
