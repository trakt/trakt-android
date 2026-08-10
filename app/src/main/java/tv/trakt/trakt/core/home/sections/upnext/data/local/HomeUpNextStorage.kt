package tv.trakt.trakt.core.home.sections.upnext.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

internal class HomeUpNextStorage : HomeUpNextLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<String, UpNextItem>()

    override suspend fun addItems(items: List<UpNextItem>) {
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

    override suspend fun setItems(items: List<UpNextItem>) {
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

    override suspend fun removeShowItems(ids: List<TraktId>) {
        mutex.withLock {
            ids.forEach { id ->
                storage.remove(getKey(id, MediaType.Show.value))
            }
        }
    }

    override suspend fun removeMovieItems(ids: List<TraktId>) {
        mutex.withLock {
            ids.forEach { id ->
                storage.remove(getKey(id, MediaType.Movie.value))
            }
        }
    }

    override suspend fun getItems(): List<UpNextItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override fun clear() {
        storage.clear()
    }

    private fun getKey(
        id: TraktId,
        type: String,
    ): String {
        return "$type-$id"
    }
}
