package tv.trakt.trakt.core.home.sections.watchlist.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.ShowItem

internal class HomeWatchlistStorage : HomeWatchlistLocalDataSource {
    private val mutex = Mutex()

    private val storageShows = mutableMapOf<String, WatchlistItem>()
    private val storageMovies = mutableMapOf<String, WatchlistItem>()

    override suspend fun setShowItems(items: List<ShowItem>) {
        mutex.withLock {
            with(storageShows) {
                clear()
                putAll(items.associateBy { it.key })
            }
        }
    }

    override suspend fun setMovieItems(items: List<MovieItem>) {
        mutex.withLock {
            with(storageMovies) {
                clear()
                putAll(items.associateBy { it.key })
            }
        }
    }

    override suspend fun getMovieItems(): List<MovieItem> {
        return mutex.withLock {
            storageMovies.values.filterIsInstance<MovieItem>()
        }
    }

    override suspend fun getShowItems(): List<ShowItem> {
        return mutex.withLock {
            storageShows.values.filterIsInstance<ShowItem>()
        }
    }

    suspend fun removeShows(showsIds: Set<TraktId>) {
        mutex.withLock {
            with(storageShows) {
                showsIds.forEach {
                    remove("${it.value}-${MediaType.SHOW.value}")
                }
            }
        }
    }

    suspend fun removeMovies(moviesIds: Set<TraktId>) {
        mutex.withLock {
            with(storageMovies) {
                moviesIds.forEach {
                    remove("${it.value}-${MediaType.MOVIE.value}")
                }
            }
        }
    }

    override fun clear() {
        storageShows.clear()
        storageMovies.clear()
    }
}
