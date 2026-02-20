package tv.trakt.trakt.core.user.data.local.liked

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal class UserLikedListsStorage : UserLikedListsLocalDataSource {
    private val mutex = Mutex()
    private var storage: MutableMap<TraktId, Instant>? = null

    private fun ensureInitialized() {
        if (storage == null) {
            storage = mutableMapOf()
        }
    }

    override suspend fun setLists(lists: Map<TraktId, Instant>) {
        mutex.withLock {
            ensureInitialized()
            storage?.let { storage ->
                storage.clear()
                storage.putAll(lists)
            }
        }
    }

    override suspend fun getLists(): Map<TraktId, Instant> {
        return mutex.withLock {
            storage ?: emptyMap()
        }
    }

    override suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    override suspend fun addList(
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

    override suspend fun removeList(listId: TraktId) {
        mutex.withLock {
            storage?.remove(listId)
        }
    }

    override fun clear() {
        storage?.clear()
        storage = null
    }
}
