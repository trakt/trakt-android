package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.sync.model.ProgressItem.MovieItem
import tv.trakt.trakt.common.core.sync.model.ProgressItem.ShowItem
import tv.trakt.trakt.common.model.TraktId

class UserProgressLocalSource {
    private val mutex = Mutex()

    private var moviesStorage: MutableMap<TraktId, MovieItem>? = null
    private var showsStorage: MutableMap<TraktId, ShowItem>? = null

    suspend fun setMovies(movies: List<MovieItem>) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableMapOf()
            }
            moviesStorage?.let { storage ->
                storage.clear()
                storage.putAll(movies.associateBy { it.mediaId })
            }
        }
    }

    suspend fun setShows(shows: List<ShowItem>) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableMapOf()
            }

            showsStorage?.let { storage ->
                storage.clear()
                storage.putAll(shows.associateBy { it.mediaId })
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

    suspend fun getShows(ids: Set<TraktId>? = null): List<ShowItem> {
        return mutex.withLock {
            showsStorage?.let { storage ->
                ids?.mapNotNull { id -> storage[id] } ?: storage.values.toList()
            } ?: emptyList()
        }
    }

    suspend fun getMovies(): List<MovieItem> {
        return mutex.withLock {
            moviesStorage?.values?.toList() ?: emptyList()
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

    fun clear() {
        moviesStorage?.clear()
        showsStorage?.clear()

        moviesStorage = null
        showsStorage = null
    }
}
