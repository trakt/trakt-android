package tv.trakt.trakt.core.lists.sections.personal.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId

internal class ListsPersonalStorage : ListsPersonalLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, CustomList>()

    override suspend fun addItems(items: List<CustomList>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.ids.trakt })
            }
        }
    }

    override suspend fun getItems(): List<CustomList> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override suspend fun deleteItem(id: TraktId) {
        mutex.withLock {
            storage.remove(id)
        }
    }

    override fun clear() {
        storage.clear()
    }
}
