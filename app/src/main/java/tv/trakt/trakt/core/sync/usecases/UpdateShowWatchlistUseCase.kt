package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class UpdateShowWatchlistUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
) {
    suspend fun addToWatchlist(showId: TraktId) {
        remoteSource.addToWatchlist(
            showId = showId,
        )
    }

    suspend fun removeFromWatchlist(showId: TraktId) {
        remoteSource.removeFromWatchlist(
            showId = showId,
        )
    }
}
