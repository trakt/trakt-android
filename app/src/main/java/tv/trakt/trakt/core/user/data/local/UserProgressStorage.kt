package tv.trakt.trakt.core.user.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.model.ProgressItem
import java.time.Instant

internal class UserProgressStorage : UserProgressLocalDataSource {
    private val mutex = Mutex()

    private var moviesStorage: MutableMap<TraktId, ProgressItem.MovieItem>? = null
    private var showsStorage: MutableMap<TraktId, ProgressItem.ShowItem>? = null

    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setMovies(
        movies: List<ProgressItem.MovieItem>,
        notify: Boolean,
    ) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableMapOf()
            }
            moviesStorage?.let { storage ->
                storage.clear()
                storage.putAll(movies.associateBy { it.mediaId })
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    override suspend fun setShows(
        shows: List<ProgressItem.ShowItem>,
        notify: Boolean,
    ) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableMapOf()
            }

            showsStorage?.let { storage ->
                storage.clear()
                storage.putAll(shows.associateBy { it.mediaId })
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    override suspend fun addMovies(
        movies: List<ProgressItem.MovieItem>,
        notify: Boolean,
    ) {
        mutex.withLock {
            if (moviesStorage == null) {
                moviesStorage = mutableMapOf()
            }
            moviesStorage?.let { storage ->
                storage.putAll(movies.associateBy { it.mediaId })
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    override suspend fun addShows(
        shows: List<ProgressItem.ShowItem>,
        notify: Boolean,
    ) {
        mutex.withLock {
            if (showsStorage == null) {
                showsStorage = mutableMapOf()
            }
            showsStorage?.let { storage ->
                storage.putAll(shows.associateBy { it.mediaId })
            }

            if (notify) {
                updatedAt.tryEmit(Instant.now())
            }
        }
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
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

    override suspend fun getAll(): List<ProgressItem> {
        return mutex.withLock {
            val movies = moviesStorage?.values ?: emptyList()
            val shows = showsStorage?.values ?: emptyList()
            (movies + shows)
        }
    }

    override suspend fun getShows(ids: Set<TraktId>?): List<ProgressItem.ShowItem> {
        return mutex.withLock {
            showsStorage?.let { storage ->
                ids?.mapNotNull { id -> storage[id] } ?: storage.values.toList()
            } ?: emptyList()
        }
    }

    override suspend fun getMovies(): List<ProgressItem.MovieItem> {
        return mutex.withLock {
            moviesStorage?.values?.toList() ?: emptyList()
        }
    }

    override suspend fun removeMovies(
        ids: Set<TraktId>,
        notify: Boolean,
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

    override suspend fun removeShows(
        ids: Set<TraktId>,
        notify: Boolean,
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

    override fun clear() {
        moviesStorage?.clear()
        showsStorage?.clear()

        moviesStorage = null
        showsStorage = null

        updatedAt.tryEmit(null)
    }
}
