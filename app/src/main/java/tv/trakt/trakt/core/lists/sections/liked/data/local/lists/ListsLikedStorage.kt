package tv.trakt.trakt.core.lists.sections.liked.data.local.lists

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal class ListsLikedStorage : ListsLikedLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, CustomList>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setLists(items: List<CustomList>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.ids.trakt })
            }
        }
    }

    override suspend fun getLists(): List<CustomList> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override suspend fun addList(list: CustomList) {
        mutex.withLock {
            storage[list.ids.trakt] = list
        }
    }

    override suspend fun removeList(listId: TraktId) {
        mutex.withLock {
            storage.remove(listId)
        }
    }

    override suspend fun notifyUpdate() {
        mutex.withLock {
            updatedAt.tryEmit(nowUtcInstant())
        }
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
    }

    override fun clear() {
        storage.clear()
        updatedAt.tryEmit(null)
    }
}
