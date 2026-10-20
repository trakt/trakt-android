package tv.trakt.trakt.app.core.details.show.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalSource
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId

internal class ChangeWatchlistUseCase(
    private val remoteSource: ShowsSyncRemoteSource,
    private val syncLocalSource: ShowsSyncLocalSource,
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
