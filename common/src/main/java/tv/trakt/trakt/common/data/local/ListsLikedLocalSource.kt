package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import java.time.Instant

class ListsLikedLocalSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, CustomList>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun setLists(items: List<CustomList>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.ids.trakt })
            }
        }
    }

    suspend fun addLists(items: List<CustomList>) {
        mutex.withLock {
            with(storage) {
                putAll(items.associateBy { it.ids.trakt })
            }
        }
    }

    suspend fun getLists(): List<CustomList> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    suspend fun addList(list: CustomList) {
        mutex.withLock {
            val existing = storage.toMap()
            storage.clear()
            storage[list.ids.trakt] = list
            storage.putAll(existing - list.ids.trakt)
        }
    }

    suspend fun removeList(listId: TraktId) {
        mutex.withLock {
            storage.remove(listId)
        }
    }

    suspend fun notifyUpdate() {
        mutex.withLock {
            updatedAt.tryEmit(nowUtcInstant())
        }
    }

    fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
    }

    fun clear() {
        storage.clear()
        updatedAt.tryEmit(null)
    }
}
