package tv.trakt.trakt.common.core.user.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.CustomListMinimal
import tv.trakt.trakt.common.model.TraktId

class UserListsStorage : UserListsLocalDataSource {
    private val mutex = Mutex()

    private var storage: MutableMap<TraktId, CustomListMinimal>? = null

    private fun ensureInitialized() {
        if (storage == null) {
            storage = mutableMapOf()
        }
    }

    override suspend fun setLists(lists: List<CustomListMinimal>) {
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

    override suspend fun getLists(): Map<TraktId, CustomListMinimal> {
        return mutex.withLock {
            storage?.toMap() ?: emptyMap()
        }
    }

    override suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    override fun clear() {
        storage?.clear()
        storage = null
    }
}
