package tv.trakt.trakt.core.user.data.local.ratings

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import java.time.Instant

internal class UserRatingsStorage : UserRatingsLocalDataSource {
    private val mutex = Mutex()

    private var showsStorage: MutableMap<TraktId, UserRating>? = null
    private var moviesStorage: MutableMap<TraktId, UserRating>? = null
    private var episodesStorage: MutableMap<TraktId, UserRating>? = null

    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setMovies(
        movies: List<UserRating>,
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
        shows: List<UserRating>,
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

    override suspend fun setEpisodes(
        episodes: List<UserRating>,
        notify: Boolean,
    ) {
        mutex.withLock {
            if (episodesStorage == null) {
                episodesStorage = mutableMapOf()
            }

            episodesStorage?.let { storage ->
                storage.clear()
                storage.putAll(episodes.associateBy { it.mediaId })
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

    override suspend fun containsEpisode(id: TraktId): Boolean {
        return mutex.withLock {
            episodesStorage?.containsKey(id) == true
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

    override suspend fun isEpisodesLoaded(): Boolean {
        return mutex.withLock {
            episodesStorage != null
        }
    }

    override suspend fun getAll(): List<UserRating> {
        return mutex.withLock {
            val movies = moviesStorage?.values ?: emptyList()
            val shows = showsStorage?.values ?: emptyList()
            val episodes = episodesStorage?.values ?: emptyList()
            (movies + shows + episodes)
        }
    }

    override suspend fun getShows(): List<UserRating> {
        return mutex.withLock {
            showsStorage?.values?.toList() ?: emptyList()
        }
    }

    override suspend fun getMovies(): List<UserRating> {
        return mutex.withLock {
            moviesStorage?.values?.toList() ?: emptyList()
        }
    }

    override suspend fun getEpisodes(): List<UserRating> {
        return mutex.withLock {
            episodesStorage?.values?.toList() ?: emptyList()
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

    override suspend fun removeEpisodes(
        ids: Set<TraktId>,
        notify: Boolean,
    ) {
        mutex.withLock {
            episodesStorage?.let { storage ->
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
        episodesStorage?.clear()

        moviesStorage = null
        showsStorage = null
        episodesStorage = null

        updatedAt.tryEmit(null)
    }
}
