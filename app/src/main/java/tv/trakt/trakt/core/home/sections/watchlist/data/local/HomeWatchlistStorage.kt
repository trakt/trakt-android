package tv.trakt.trakt.core.home.sections.watchlist.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

internal class HomeWatchlistStorage : HomeWatchlistLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<String, WatchlistItem>()

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

    suspend fun removeShows(showsIds: Set<TraktId>) {
        mutex.withLock {
            with(storage) {
                showsIds.forEach {
                    remove("${it.value}-show")
                }
            }
        }
    }

    suspend fun removeMovies(moviesIds: Set<TraktId>) {
        mutex.withLock {
            with(storage) {
                moviesIds.forEach {
                    remove("${it.value}-movie")
                }
            }
        }
    }

    override fun clear() {
        storage.clear()
    }
}
