package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

class HomeUpNextLocalSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<String, UpNextItem>()

    suspend fun addItems(items: List<UpNextItem>) {
        mutex.withLock {
            with(storage) {
                putAll(
                    items.associateBy {
                        getKey(it.id, it.type.value)
                    },
                )
            }
        }
    }

    suspend fun setItems(items: List<UpNextItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(
                    items.associateBy {
                        getKey(it.id, it.type.value)
                    },
                )
            }
        }
    }

    suspend fun removeShowItems(ids: List<TraktId>) {
        mutex.withLock {
            ids.forEach { id ->
                storage.remove(getKey(id, MediaType.Show.value))
            }
        }
    }

    suspend fun removeMovieItems(ids: List<TraktId>) {
        mutex.withLock {
            ids.forEach { id ->
                storage.remove(getKey(id, MediaType.Movie.value))
            }
        }
    }

    suspend fun getItems(): List<UpNextItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    fun clear() {
        storage.clear()
    }

    private fun getKey(
        id: TraktId,
        type: String,
    ): String {
        return "$type-$id"
    }
}
