package tv.trakt.trakt.core.user.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import java.time.Instant

internal class UserListsStorage : UserListsLocalDataSource {
    private val mutex = Mutex()

    private var storage: MutableMap<TraktId, List<PersonalListItem>>? = null
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private fun ensureInitialized() {
        if (storage == null) {
            storage = mutableMapOf()
        }
    }

    override suspend fun setLists(
        lists: Map<TraktId, List<PersonalListItem>>,
        notify: Boolean,
    ) {
        mutex.withLock {
            ensureInitialized()
            storage?.let { storage ->
                storage.clear()
                storage.putAll(lists)
            }

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun addLists(
        lists: Map<TraktId, List<PersonalListItem>>,
        notify: Boolean,
    ) {
        mutex.withLock {
            ensureInitialized()
            storage?.putAll(lists)

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun containsList(listId: TraktId): Boolean {
        return mutex.withLock {
            storage?.containsKey(listId) == true
        }
    }

    override suspend fun getList(listId: TraktId): List<PersonalListItem>? {
        return mutex.withLock {
            storage?.get(listId)
        }
    }

    override suspend fun getLists(): Map<TraktId, List<PersonalListItem>> {
        return mutex.withLock {
            storage ?: emptyMap()
        }
    }

    override suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    override suspend fun removeLists(
        listsIds: Set<TraktId>,
        notify: Boolean,
    ) {
        mutex.withLock {
            storage?.let { storage ->
                listsIds.forEach { id ->
                    storage.remove(id)
                }
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt
    }

    override fun clear() {
        storage?.clear()
        updatedAt.tryEmit(null)
    }
}
