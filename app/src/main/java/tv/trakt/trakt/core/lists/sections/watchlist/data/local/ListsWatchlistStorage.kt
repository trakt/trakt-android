package tv.trakt.trakt.core.lists.sections.watchlist.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

internal class ListsWatchlistStorage : ListsWatchlistLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, WatchlistItem>()

    override suspend fun addItems(items: List<WatchlistItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.id })
            }
        }
    }

    override suspend fun getItems(): List<WatchlistItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override fun clear() {
        storage.clear()
    }
}
