package tv.trakt.trakt.core.home.sections.upnext.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import java.time.Instant

internal class HomeUpNextStorage : HomeUpNextLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, ProgressShow>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun addItems(
        items: List<ProgressShow>,
        notify: Boolean,
    ) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.show.ids.trakt })
            }
            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun removeItems(
        showIds: List<TraktId>,
        notify: Boolean,
    ) {
        mutex.withLock {
            showIds.forEach { id ->
                storage.remove(id)
            }
            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun getItems(): List<ProgressShow> {
        return mutex.withLock {
            storage.values.toList()
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
