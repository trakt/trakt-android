package tv.trakt.trakt.app.core.sync.data.local.shows

import tv.trakt.trakt.app.core.sync.model.WatchedShow
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

internal interface ShowsSyncLocalDataSource {
    suspend fun saveWatchlist(
        showsIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    )

    suspend fun removeWatchlist(
        showsIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    )

    suspend fun saveWatched(
        shows: List<WatchedShow>,
        timestamp: ZonedDateTime?,
    )

    suspend fun getWatchlist(): Set<TraktId>?

    suspend fun getWatched(): Map<TraktId, WatchedShow>?

    suspend fun getWatchlistUpdatedAt(): ZonedDateTime?

    suspend fun getWatchedUpdatedAt(): ZonedDateTime?

    suspend fun clear()
}
