package tv.trakt.trakt.core.user.data.local.library

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.library.model.LibraryItem

internal class UserLibraryStorage : UserLibraryLocalDataSource {
    private val mutex = Mutex()

    private var moviesStorage: MutableMap<TraktId, LibraryItem>? = null
    private var episodesStorage: MutableMap<TraktId, LibraryItem>? = null

    override suspend fun setMovies(movies: List<LibraryItem.MovieItem>) {
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

    override suspend fun getMovies(): List<LibraryItem.MovieItem> {
        return mutex.withLock {
            moviesStorage?.values
                ?.filterIsInstance<LibraryItem.MovieItem>()
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
        }
    }

    override suspend fun isMoviesLoaded(): Boolean {
        return mutex.withLock {
            moviesStorage != null
        }
    }

    override suspend fun setEpisodes(episodes: List<LibraryItem.EpisodeItem>) {
        mutex.withLock {
            if (episodesStorage == null) {
                episodesStorage = mutableMapOf()
            }
            episodesStorage?.let { storage ->
                storage.clear()
                storage.putAll(episodes.associateBy { it.id })
            }
        }
    }

    override suspend fun getEpisodes(): List<LibraryItem.EpisodeItem> {
        return mutex.withLock {
            episodesStorage?.values
                ?.filterIsInstance<LibraryItem.EpisodeItem>()
                ?: emptyList()
        }
    }

    override suspend fun isEpisodesLoaded(): Boolean {
        return mutex.withLock {
            episodesStorage != null
        }
    }

    override suspend fun removeEpisodes(ids: Set<TraktId>) {
        mutex.withLock {
            episodesStorage?.let { storage ->
                ids.forEach { id ->
                    storage.remove(id)
                }
            }
        }
    }

    override suspend fun getAll(): List<LibraryItem> {
        return mutex.withLock {
            val movies = moviesStorage?.values ?: emptyList()
            val episodes = episodesStorage?.values ?: emptyList()
            movies + episodes
        }
    }

    override fun clear() {
        moviesStorage?.clear()
        episodesStorage?.clear()

        moviesStorage = null
        episodesStorage = null
    }
}
