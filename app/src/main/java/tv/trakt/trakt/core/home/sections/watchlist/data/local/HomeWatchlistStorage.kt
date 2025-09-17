package tv.trakt.trakt.core.home.sections.watchlist.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.watchlist.model.WatchlistMovie
import java.time.Instant

internal class HomeWatchlistStorage : HomeWatchlistLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, WatchlistMovie>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun addItems(items: List<WatchlistMovie>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.movie.ids.trakt })
            }
            updatedAt.tryEmit(nowUtcInstant())
        }
    }

    override suspend fun removeItems(ids: Set<TraktId>) {
        mutex.withLock {
            ids.forEach {
                storage.remove(it)
            }
            updatedAt.tryEmit(nowUtcInstant())
        }
    }

    override suspend fun getItems(): List<WatchlistMovie> {
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
