package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomListMinimal

class UserListsLocalSource {
    private val mutex = Mutex()

    private var storage: MutableMap<TraktId, CustomListMinimal>? = null

    private fun ensureInitialized() {
        if (storage == null) {
            storage = mutableMapOf()
        }
    }

    suspend fun setLists(lists: List<CustomListMinimal>) {
        mutex.withLock {
            ensureInitialized()
            storage?.let { storage ->
                storage.clear()
                lists.forEach { list ->
                    storage[list.id] = list
                }
            }
        }
    }

    suspend fun getLists(): Map<TraktId, CustomListMinimal> {
        return mutex.withLock {
            storage?.toMap() ?: emptyMap()
        }
    }

    suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    fun clear() {
        storage?.clear()
        storage = null
    }
}
