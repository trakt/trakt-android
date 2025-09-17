package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource

internal class UpdateMovieWatchlistUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
) {
    suspend fun removeFromWatchlist(movieId: TraktId) {
        remoteSource.removeFromWatchlist(
            movieId = movieId,
        )
    }
}
