package tv.trakt.trakt.core.lists.sections.personal.data.local

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
import java.time.ZonedDateTime

internal class ListsPersonalStorage : ListsPersonalLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, CustomList>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setItems(items: List<CustomList>) {
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

    override suspend fun editItem(
        listId: TraktId,
        name: String,
        description: String?,
        notify: Boolean,
    ) {
        mutex.withLock {
            val existing = storage[listId] ?: return

            storage[listId] = existing.copy(
                name = name,
                description = description,
            )

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun deleteItem(
        id: TraktId,
        notify: Boolean,
    ) {
        mutex.withLock {
            storage.remove(id)
        }
        if (notify) {
            updatedAt.tryEmit(nowUtcInstant())
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

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
    }

    override fun clear() {
        storage.clear()
        updatedAt.tryEmit(null)
    }
}
