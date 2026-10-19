package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

class UserFavoritesLocalSource {
    private val mutex = Mutex()

    private var moviesStorage: MutableMap<TraktId, FavoriteItem>? = null
    private var showsStorage: MutableMap<TraktId, FavoriteItem>? = null

    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun setMovies(
        movies: List<FavoriteItem.MovieItem>,
        notify: Boolean = false,
    ) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableMapOf()
            }
            moviesStorage?.let { storage ->
                storage.clear()
                storage.putAll(movies.associateBy { it.id })
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    suspend fun setShows(
        shows: List<FavoriteItem.ShowItem>,
        notify: Boolean = false,
    ) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableMapOf()
            }

            showsStorage?.let { storage ->
                storage.clear()
                storage.putAll(shows.associateBy { it.id })
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    suspend fun addMovies(
        movies: List<FavoriteItem.MovieItem>,
        notify: Boolean = false,
    ) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableMapOf()
            }
            moviesStorage?.let { storage ->
                storage.putAll(movies.associateBy { it.id })
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    suspend fun addShows(
        shows: List<FavoriteItem.ShowItem>,
        notify: Boolean = false,
    ) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableMapOf()
            }
            showsStorage?.let { storage ->
                storage.putAll(shows.associateBy { it.id })
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
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

    suspend fun getAll(): List<FavoriteItem> {
        return mutex.withLock {
            val movies = moviesStorage?.values ?: emptyList()
            val shows = showsStorage?.values ?: emptyList()
            (movies + shows)
        }
    }

    suspend fun getShows(): List<FavoriteItem.ShowItem> {
        return mutex.withLock {
            showsStorage?.values
                ?.filterIsInstance<FavoriteItem.ShowItem>()
                ?: emptyList()
        }
    }

    suspend fun getMovies(): List<FavoriteItem.MovieItem> {
        return mutex.withLock {
            moviesStorage?.values
                ?.filterIsInstance<FavoriteItem.MovieItem>()
                ?: emptyList()
        }
    }

    suspend fun removeMovies(
        ids: Set<TraktId>,
        notify: Boolean = false,
    ) {
        mutex.withLock {
            moviesStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    suspend fun removeShows(
        ids: Set<TraktId>,
        notify: Boolean = false,
    ) {
        mutex.withLock {
            showsStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    fun clear() {
        moviesStorage?.clear()
        showsStorage?.clear()

        moviesStorage = null
        showsStorage = null

        updatedAt.tryEmit(null)
    }
}
