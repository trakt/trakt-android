package tv.trakt.trakt.common.core.user.data.local.watchlist

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.home.watchlist.data.HomeWatchlistStorage
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.model.TraktId

class UserWatchlistStorage(
    private val homeWatchlistStorage: HomeWatchlistStorage,
) : UserWatchlistLocalDataSource {
    private val mutex = Mutex()

    private var moviesStorage: MutableMap<TraktId, WatchlistItem>? = null
    private var showsStorage: MutableMap<TraktId, WatchlistItem>? = null

    override suspend fun setMovies(movies: List<WatchlistItem.MovieItem>) {
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

    override suspend fun setShows(shows: List<WatchlistItem.ShowItem>) {
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

    override suspend fun addMovies(movies: List<WatchlistItem.MovieItem>) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableMapOf()
            }
            moviesStorage?.let { storage ->
                storage.putAll(movies.associateBy { it.id })
            }
        }
    }

    override suspend fun addShows(shows: List<WatchlistItem.ShowItem>) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableMapOf()
            }
            showsStorage?.let { storage ->
                storage.putAll(shows.associateBy { it.id })
            }
        }
    }

    override suspend fun containsShow(id: TraktId): Boolean {
        return mutex.withLock {
            showsStorage?.containsKey(id) == true
        }
    }

    override suspend fun containsMovie(id: TraktId): Boolean {
        return mutex.withLock {
            moviesStorage?.containsKey(id) == true
        }
    }

    override suspend fun isMoviesLoaded(): Boolean {
        return mutex.withLock {
            moviesStorage != null
        }
    }

    override suspend fun isShowsLoaded(): Boolean {
        return mutex.withLock {
            showsStorage != null
        }
    }

    override suspend fun getAll(): List<WatchlistItem> {
        return mutex.withLock {
            val movies = moviesStorage?.values ?: emptyList()
            val shows = showsStorage?.values ?: emptyList()
            (movies + shows)
        }
    }

    override suspend fun getShows(): List<WatchlistItem.ShowItem> {
        return mutex.withLock {
            showsStorage?.values
                ?.filterIsInstance<WatchlistItem.ShowItem>()
                ?: emptyList()
        }
    }

    override suspend fun getMovies(): List<WatchlistItem.MovieItem> {
        return mutex.withLock {
            moviesStorage?.values
                ?.filterIsInstance<WatchlistItem.MovieItem>()
                ?: emptyList()
        }
    }

    override suspend fun removeMovies(ids: Set<TraktId>) {
        mutex.withLock {
            moviesStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }

            homeWatchlistStorage.removeMovies(ids)
        }
    }

    override suspend fun removeShows(ids: Set<TraktId>) {
        mutex.withLock {
            showsStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }

            homeWatchlistStorage.removeShows(ids)
        }
    }

    override fun clear() {
        moviesStorage?.clear()
        showsStorage?.clear()

        moviesStorage = null
        showsStorage = null

        homeWatchlistStorage.clear()
    }
}
