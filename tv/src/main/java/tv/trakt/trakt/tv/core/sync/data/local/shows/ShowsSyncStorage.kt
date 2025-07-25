package tv.trakt.trakt.tv.core.sync.data.local.shows

import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.core.sync.model.WatchedShow
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

internal class ShowsSyncStorage : ShowsSyncLocalDataSource {
    private val mutex = Mutex()

    private var watchlistCache: MutableSet<TraktId>? = null
    private var watchedCache: MutableMap<TraktId, WatchedShow>? = null

    private var watchlistUpdatedAt: ZonedDateTime? = null
    private var watchedUpdatedAt: ZonedDateTime? = null

    // Watchlist

    override suspend fun getWatchlist(): Set<TraktId>? {
        return mutex.withLock {
            watchlistCache?.toSet()
        }
    }

    override suspend fun saveWatchlist(
        showsIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    ) {
        mutex.withLock {
            if (showsIds.isEmpty()) {
                return@withLock
            }

            if (watchlistCache == null) {
                watchlistCache = ConcurrentSet()
            }

            watchlistCache?.addAll(showsIds)
            timestamp?.let {
                watchlistUpdatedAt = it
            }
        }
    }

    override suspend fun removeWatchlist(
        showsIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    ) {
        mutex.withLock {
            if (showsIds.isEmpty() || watchlistCache == null) {
                return@withLock
            }
            watchlistCache?.removeAll(showsIds)
            timestamp?.let {
                watchlistUpdatedAt = it
            }
        }
    }

    override suspend fun getWatchlistUpdatedAt(): ZonedDateTime? {
        return mutex.withLock {
            watchlistUpdatedAt
        }
    }

    // Watched

    override suspend fun getWatched(): Map<TraktId, WatchedShow>? {
        return mutex.withLock {
            watchedCache?.toMap()
        }
    }

    override suspend fun saveWatched(
        shows: List<WatchedShow>,
        timestamp: ZonedDateTime?,
    ) {
        mutex.withLock {
            if (shows.isEmpty()) {
                return@withLock
            }

            if (watchedCache == null) {
                watchedCache = ConcurrentHashMap<TraktId, WatchedShow>()
            }

            watchedCache?.run {
                val ids = shows.map { m -> m.showId }
                entries.removeAll { it.key in ids }
                putAll(shows.associateBy { it.showId })
            }
            timestamp?.let {
                watchedUpdatedAt = it
            }
        }
    }

    override suspend fun getWatchedUpdatedAt(): ZonedDateTime? {
        return mutex.withLock {
            watchedUpdatedAt
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            watchlistCache = null
            watchedCache = null

            watchlistUpdatedAt = null
            watchedUpdatedAt = null
        }
    }
}
