package tv.trakt.trakt.app.core.details.movie.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.app.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId

internal class ChangeWatchlistUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val syncLocalSource: MoviesSyncLocalDataSource,
) {
    suspend fun removeFromWatchlist(movieId: TraktId) {
        remoteSource.removeFromWatchlist(movieId)
        syncLocalSource.removeWatchlist(setOf(movieId), nowUtc())
    }

    suspend fun addToWatchlist(movieId: TraktId) {
        remoteSource.addToWatchlist(movieId)
        syncLocalSource.saveWatchlist(setOf(movieId), nowUtc())
    }
}
