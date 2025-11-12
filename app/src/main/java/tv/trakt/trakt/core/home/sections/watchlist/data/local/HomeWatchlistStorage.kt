package tv.trakt.trakt.core.home.sections.watchlist.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import java.time.Instant

internal class HomeWatchlistStorage : HomeWatchlistLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<String, WatchlistItem>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun addItems(items: List<WatchlistItem>) {
        mutex.withLock {
            with(storage) {
                putAll(items.associateBy { it.key })
            }
        }
    }

    override suspend fun setItems(items: List<WatchlistItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.key })
            }
        }
    }

    override suspend fun getItems(): List<WatchlistItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override suspend fun removeItems(itemsKeys: List<String>) {
        mutex.withLock {
            with(storage) {
                itemsKeys.forEach {
                    remove(it)
                }
            }
        }
    }

    override fun notifyUpdate() {
        updatedAt.tryEmit(nowUtcInstant())
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
    }

    override fun clear() {
        storage.clear()
        updatedAt.tryEmit(null)
    }
}
