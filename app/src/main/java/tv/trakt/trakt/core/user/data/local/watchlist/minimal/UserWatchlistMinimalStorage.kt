package tv.trakt.trakt.core.user.data.local.watchlist.minimal

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal class UserWatchlistMinimalStorage(
//    private val homeWatchlistStorage: HomeWatchlistStorage,
) : UserWatchlistMinimalLocalDataSource {
    private val mutex = Mutex()

    private var moviesStorage: MutableSet<TraktId>? = null
    private var showsStorage: MutableSet<TraktId>? = null

    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setMovies(ids: Set<TraktId>) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableSetOf()
            }
            moviesStorage?.let { storage ->
                storage.clear()
                storage.addAll(ids)
            }
        }
    }

    override suspend fun setShows(ids: Set<TraktId>) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableSetOf()
            }
            showsStorage?.let { storage ->
                storage.clear()
                storage.addAll(ids)
            }
        }
    }

    override suspend fun addMovies(movies: Set<TraktId>) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableSetOf()
            }
            moviesStorage?.addAll(movies)
        }
    }

    override suspend fun addShows(shows: Set<TraktId>) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableSetOf()
            }
            showsStorage?.addAll(shows)
        }
    }

    override suspend fun containsShow(id: TraktId): Boolean {
        return mutex.withLock {
            showsStorage?.contains(id) == true
        }
    }

    override suspend fun containsMovie(id: TraktId): Boolean {
        return mutex.withLock {
            moviesStorage?.contains(id) == true
        }
    }

    override suspend fun getShows(): Set<TraktId> {
        return mutex.withLock {
            showsStorage?.toSet() ?: emptySet()
        }
    }

    override suspend fun getMovies(): Set<TraktId> {
        return mutex.withLock {
            moviesStorage?.toSet() ?: emptySet()
        }
    }

    override suspend fun removeMovies(ids: Set<TraktId>) {
        mutex.withLock {
            moviesStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }

//            homeWatchlistStorage.removeMovies(ids)
        }
    }

    override suspend fun removeShows(ids: Set<TraktId>) {
        mutex.withLock {
            showsStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }

//            homeWatchlistStorage.removeShows(ids)
        }
    }

    override suspend fun isShowsLoaded(): Boolean {
        return mutex.withLock {
            showsStorage != null
        }
    }

    override suspend fun isMoviesLoaded(): Boolean {
        return mutex.withLock {
            moviesStorage != null
        }
    }

    override fun clear() {
        moviesStorage?.clear()
        showsStorage?.clear()

        moviesStorage = null
        showsStorage = null

//        homeWatchlistStorage.clear()

        updatedAt.tryEmit(null)
    }
}
