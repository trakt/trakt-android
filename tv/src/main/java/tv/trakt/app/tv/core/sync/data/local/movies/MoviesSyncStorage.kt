package tv.trakt.app.tv.core.sync.data.local.movies

import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.sync.model.WatchedMovie
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

internal class MoviesSyncStorage : MoviesSyncLocalDataSource {
    private val mutex = Mutex()

    private var watchlistCache: MutableSet<TraktId>? = null
    private var watchedCache: MutableMap<TraktId, WatchedMovie>? = null

    private var watchlistUpdatedAt: ZonedDateTime? = null
    private var watchedUpdatedAt: ZonedDateTime? = null

    // Watchlist

    override suspend fun getWatchlist(): Set<TraktId>? {
        return mutex.withLock {
            watchlistCache?.toSet()
        }
    }

    override suspend fun saveWatchlist(
        moviesIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    ) {
        mutex.withLock {
            if (moviesIds.isEmpty()) {
                return@withLock
            }

            if (watchlistCache == null) {
                watchlistCache = ConcurrentSet()
            }

            watchlistCache?.addAll(moviesIds)
            timestamp?.let {
                watchlistUpdatedAt = it
            }
        }
    }

    override suspend fun removeWatchlist(
        moviesIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    ) {
        mutex.withLock {
            if (moviesIds.isEmpty() || watchlistCache == null) {
                return@withLock
            }
            watchlistCache?.removeAll(moviesIds)
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

    override suspend fun getWatched(): Map<TraktId, WatchedMovie>? {
        return mutex.withLock {
            watchedCache?.toMap()
        }
    }

    override suspend fun saveWatched(
        movies: List<WatchedMovie>,
        timestamp: ZonedDateTime?,
    ) {
        mutex.withLock {
            if (movies.isEmpty()) {
                return@withLock
            }

            if (watchedCache == null) {
                watchedCache = ConcurrentHashMap<TraktId, WatchedMovie>()
            }

            watchedCache?.run {
                val ids = movies.map { m -> m.movieId }
                entries.removeAll { it.key in ids }
                putAll(movies.associateBy { it.movieId })
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
