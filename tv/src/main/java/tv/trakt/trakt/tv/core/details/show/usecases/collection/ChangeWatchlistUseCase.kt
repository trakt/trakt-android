package tv.trakt.trakt.tv.core.details.show.usecases.collection

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.tv.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.tv.helpers.extensions.nowUtc

internal class ChangeWatchlistUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
    private val syncLocalSource: ShowsSyncLocalDataSource,
) {
    suspend fun removeFromWatchlist(showId: TraktId) {
        remoteSource.removeFromWatchlist(showId)
        syncLocalSource.removeWatchlist(setOf(showId), nowUtc())
    }

    suspend fun addToWatchlist(showId: TraktId) {
        remoteSource.addToWatchlist(showId)
        syncLocalSource.saveWatchlist(setOf(showId), nowUtc())
    }
}
