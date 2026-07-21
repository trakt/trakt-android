package tv.trakt.trakt.common.core.user.data.local.watchlist

import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface WatchlistUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant>

    enum class Source {
        Default,
        AllWatchlist,
    }
}
