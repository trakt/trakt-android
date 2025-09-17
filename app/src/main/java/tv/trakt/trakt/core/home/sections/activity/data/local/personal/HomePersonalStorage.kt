package tv.trakt.trakt.core.home.sections.activity.data.local.personal

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import java.time.Instant

internal class HomePersonalStorage : HomePersonalLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<Long, HomeActivityItem>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun addItems(
        items: List<HomeActivityItem>,
        ignoreUpdate: Boolean,
    ) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.id })
            }
            if (!ignoreUpdate) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun getItems(): List<HomeActivityItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override suspend fun removeItems(
        ids: Set<Long>,
        ignoreUpdate: Boolean,
    ) {
        mutex.withLock {
            ids.forEach {
                storage.remove(it)
            }
            if (!ignoreUpdate) {
                updatedAt.tryEmit(nowUtcInstant())
            }
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
