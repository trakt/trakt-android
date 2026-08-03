package tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists

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
import java.time.ZonedDateTime

internal class ListsCollaborationsStorage : ListsCollaborationsLocalDataSource {
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

    override suspend fun onUpdatedAt(
        id: TraktId,
        updatedAt: ZonedDateTime,
    ) {
        mutex.withLock {
            val existing = storage[id] ?: return
            storage[id] = existing.copy(
                updatedAt = updatedAt,
            )
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
