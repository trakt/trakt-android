package tv.trakt.trakt.core.movies.sections.hot.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.model.WatchersMovie
import java.time.Instant

internal class HotMoviesStorage : HotMoviesLocalDataSource {
    private val mutex = Mutex()
    private val moviesCache = mutableMapOf<TraktId, WatchersMovie>()

    override suspend fun addMovies(
        movies: List<WatchersMovie>,
        addedAt: Instant,
    ) {
        mutex.withLock {
            with(moviesCache) {
                clear()
                putAll(movies.associateBy { it.movie.ids.trakt })
            }
        }
    }

    override suspend fun getMovies(): List<WatchersMovie> {
        return mutex.withLock {
            moviesCache.values.toList()
        }
    }
}
