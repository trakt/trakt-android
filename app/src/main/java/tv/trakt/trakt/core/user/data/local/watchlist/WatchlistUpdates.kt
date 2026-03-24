package tv.trakt.trakt.core.user.data.local.watchlist

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface WatchlistUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant>

    enum class Source {
        Default,
        AllWatchlist,
    }
}
