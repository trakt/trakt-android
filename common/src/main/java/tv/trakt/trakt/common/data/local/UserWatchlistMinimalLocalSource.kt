package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId

class UserWatchlistMinimalLocalSource {
    private val mutex = Mutex()

    private var moviesStorage: MutableSet<TraktId>? = null
    private var showsStorage: MutableSet<TraktId>? = null

    suspend fun setMovies(ids: Set<TraktId>) {
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

    suspend fun setShows(ids: Set<TraktId>) {
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

    suspend fun addMovies(movies: Set<TraktId>) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableSetOf()
            }
            moviesStorage?.addAll(movies)
        }
    }

    suspend fun addShows(shows: Set<TraktId>) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableSetOf()
            }
            showsStorage?.addAll(shows)
        }
    }

    suspend fun containsShow(id: TraktId): Boolean {
        return mutex.withLock {
            showsStorage?.contains(id) == true
        }
    }

    suspend fun containsMovie(id: TraktId): Boolean {
        return mutex.withLock {
            moviesStorage?.contains(id) == true
        }
    }

    suspend fun getShows(): Set<TraktId> {
        return mutex.withLock {
            showsStorage?.toSet() ?: emptySet()
        }
    }

    suspend fun getMovies(): Set<TraktId> {
        return mutex.withLock {
            moviesStorage?.toSet() ?: emptySet()
        }
    }

    suspend fun removeMovies(ids: Set<TraktId>) {
        mutex.withLock {
            moviesStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }
        }
    }

    suspend fun removeShows(ids: Set<TraktId>) {
        mutex.withLock {
            showsStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }
        }
    }

    suspend fun isShowsLoaded(): Boolean {
        return mutex.withLock {
            showsStorage != null
        }
    }

    suspend fun isMoviesLoaded(): Boolean {
        return mutex.withLock {
            moviesStorage != null
        }
    }

    fun clear() {
        moviesStorage?.clear()
        showsStorage?.clear()

        moviesStorage = null
        showsStorage = null
    }
}
