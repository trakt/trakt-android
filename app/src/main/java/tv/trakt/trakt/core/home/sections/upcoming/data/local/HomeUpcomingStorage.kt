package tv.trakt.trakt.core.home.sections.upcoming.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import java.time.Instant

internal class HomeUpcomingStorage : HomeUpcomingLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, HomeUpcomingItem>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setItems(items: List<HomeUpcomingItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.id })
            }
        }
    }

    override suspend fun getItems(): List<HomeUpcomingItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override suspend fun removeShowItems(
        showIds: List<TraktId>,
        notify: Boolean,
    ) {
        mutex.withLock {
            val toRemove = storage.values
                .filterIsInstance<HomeUpcomingItem.EpisodeItem>()
                .filter { it.show.ids.trakt in showIds }
                .map { it.id }

            toRemove.forEach { id ->
                if (storage[id] is HomeUpcomingItem.EpisodeItem) {
                    storage.remove(id)
                }
            }

            if (notify) {
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
