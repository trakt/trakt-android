package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

class UserLikedListsLocalSource {
    private val mutex = Mutex()
    private var storage: MutableMap<TraktId, Instant>? = null

    private fun ensureInitialized() {
        if (storage == null) {
            storage = mutableMapOf()
        }
    }

    suspend fun setLists(lists: Map<TraktId, Instant>) {
        mutex.withLock {
            ensureInitialized()
            storage?.let { storage ->
                storage.clear()
                storage.putAll(lists)
            }
        }
    }

    suspend fun getLists(): Map<TraktId, Instant> {
        return mutex.withLock {
            storage ?: emptyMap()
        }
    }

    suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    suspend fun addList(
        listId: TraktId,
        likedAt: Instant,
    ) {
        mutex.withLock {
            ensureInitialized()
            storage?.let { storage ->
                storage[listId] = likedAt
            }
        }
    }

    suspend fun removeList(listId: TraktId) {
        mutex.withLock {
            storage?.remove(listId)
        }
    }

    fun clear() {
        storage?.clear()
        storage = null
    }
}
