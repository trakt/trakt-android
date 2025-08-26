package tv.trakt.trakt.core.home.sections.watchlist.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.watchlist.model.WatchlistMovie

internal class HomeWatchlistStorage : HomeWatchlistLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, WatchlistMovie>()

    override suspend fun addItems(items: List<WatchlistMovie>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.movie.ids.trakt })
            }
        }
    }

    override suspend fun getItems(): List<WatchlistMovie> {
        return mutex.withLock {
            storage.values.toList()
        }
    }
}
