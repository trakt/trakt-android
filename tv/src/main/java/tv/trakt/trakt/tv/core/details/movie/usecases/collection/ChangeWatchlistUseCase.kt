package tv.trakt.trakt.tv.core.details.movie.usecases.collection

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.tv.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.tv.helpers.extensions.nowUtc

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
