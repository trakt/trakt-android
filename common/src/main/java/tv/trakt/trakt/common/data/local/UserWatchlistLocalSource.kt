package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.data.local.HomeWatchlistLocalSource
import tv.trakt.trakt.common.model.TraktId

class UserWatchlistLocalSource(
    private val homeWatchlistLocalSource: HomeWatchlistLocalSource,
) {
    private val mutex = Mutex()

    private var moviesStorage: MutableMap<TraktId, WatchlistItem>? = null
    private var showsStorage: MutableMap<TraktId, WatchlistItem>? = null

    suspend fun setMovies(movies: List<WatchlistItem.MovieItem>) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableMapOf()
            }
            moviesStorage?.let { storage ->
                storage.clear()
                storage.putAll(movies.associateBy { it.id })
            }
        }
    }

    suspend fun setShows(shows: List<WatchlistItem.ShowItem>) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableMapOf()
            }

            showsStorage?.let { storage ->
                storage.clear()
                storage.putAll(shows.associateBy { it.id })
            }
        }
    }

    suspend fun addMovies(movies: List<WatchlistItem.MovieItem>) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableMapOf()
            }
            moviesStorage?.let { storage ->
                storage.putAll(movies.associateBy { it.id })
            }
        }
    }

    suspend fun addShows(shows: List<WatchlistItem.ShowItem>) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableMapOf()
            }
            showsStorage?.let { storage ->
                storage.putAll(shows.associateBy { it.id })
            }
        }
    }

    suspend fun containsShow(id: TraktId): Boolean {
        return mutex.withLock {
            showsStorage?.containsKey(id) == true
        }
    }

    suspend fun containsMovie(id: TraktId): Boolean {
        return mutex.withLock {
            moviesStorage?.containsKey(id) == true
        }
    }

    suspend fun isMoviesLoaded(): Boolean {
        return mutex.withLock {
            moviesStorage != null
        }
    }

    suspend fun isShowsLoaded(): Boolean {
        return mutex.withLock {
            showsStorage != null
        }
    }

    suspend fun getAll(): List<WatchlistItem> {
        return mutex.withLock {
            val movies = moviesStorage?.values ?: emptyList()
            val shows = showsStorage?.values ?: emptyList()
            (movies + shows)
        }
    }

    suspend fun getShows(): List<WatchlistItem.ShowItem> {
        return mutex.withLock {
            showsStorage?.values
                ?.filterIsInstance<WatchlistItem.ShowItem>()
                ?: emptyList()
        }
    }

    suspend fun getMovies(): List<WatchlistItem.MovieItem> {
        return mutex.withLock {
            moviesStorage?.values
                ?.filterIsInstance<WatchlistItem.MovieItem>()
                ?: emptyList()
        }
    }

    suspend fun removeMovies(ids: Set<TraktId>) {
        mutex.withLock {
            moviesStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }

            homeWatchlistLocalSource.removeMovies(ids)
        }
    }

    suspend fun removeShows(ids: Set<TraktId>) {
        mutex.withLock {
            showsStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }

            homeWatchlistLocalSource.removeShows(ids)
        }
    }

    fun clear() {
        moviesStorage?.clear()
        showsStorage?.clear()

        moviesStorage = null
        showsStorage = null

        homeWatchlistLocalSource.clear()
    }
}
