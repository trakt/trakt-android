package tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.model.DiscoverItem
import java.time.Instant

internal class AnticipatedMoviesStorage : AnticipatedMoviesLocalDataSource {
    private val mutex = Mutex()
    private val moviesCache = mutableMapOf<TraktId, DiscoverItem.MovieItem>()

    override suspend fun addMovies(
        movies: List<DiscoverItem.MovieItem>,
        addedAt: Instant,
    ) {
        mutex.withLock {
            with(moviesCache) {
                clear()
                putAll(movies.associateBy { it.movie.ids.trakt })
            }
        }
    }

    override suspend fun getMovies(): List<DiscoverItem.MovieItem> {
        return mutex.withLock {
            moviesCache.values.toList()
        }
    }
}
