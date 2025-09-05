package tv.trakt.trakt.app.core.details.show.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId

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
