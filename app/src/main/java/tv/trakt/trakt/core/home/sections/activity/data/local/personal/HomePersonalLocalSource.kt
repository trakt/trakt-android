package tv.trakt.trakt.core.home.sections.activity.data.local.personal

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import java.time.Instant

internal class HomePersonalLocalSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<Long, HomeActivityItem>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun addItems(
        items: List<HomeActivityItem>,
        notify: Boolean,
    ) {
        mutex.withLock {
            with(storage) {
                putAll(items.associateBy { it.id })
            }
            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    suspend fun setItems(
        items: List<HomeActivityItem>,
        notify: Boolean,
    ) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.id })
            }
            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    suspend fun getItems(): List<HomeActivityItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    suspend fun removeItems(
        ids: Set<Long>,
        notify: Boolean,
    ) {
        mutex.withLock {
            ids.forEach {
                storage.remove(it)
            }
            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
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
