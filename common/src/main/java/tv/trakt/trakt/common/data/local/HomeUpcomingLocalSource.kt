package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.calendar.CalendarItem
import java.time.Instant

class HomeUpcomingLocalSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, CalendarItem>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun setItems(items: List<CalendarItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.id })
            }
        }
    }

    suspend fun getItems(): List<CalendarItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    suspend fun removeShowItems(
        showIds: List<TraktId>,
        notify: Boolean,
    ) {
        mutex.withLock {
            val toRemove = storage.values
                .filterIsInstance<CalendarItem.EpisodeItem>()
                .filter { it.show.ids.trakt in showIds }
                .map { it.id }

            toRemove.forEach { id ->
                if (storage[id] is CalendarItem.EpisodeItem) {
                    storage.remove(id)
                }
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
