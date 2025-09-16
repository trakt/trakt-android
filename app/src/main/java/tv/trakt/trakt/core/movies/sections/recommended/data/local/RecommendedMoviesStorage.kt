package tv.trakt.trakt.core.movies.sections.recommended.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal class RecommendedMoviesStorage : RecommendedMoviesLocalDataSource {
    private val mutex = Mutex()
    private val moviesCache = mutableMapOf<TraktId, Movie>()

    override suspend fun addMovies(
        movies: List<Movie>,
        addedAt: Instant,
    ) {
        mutex.withLock {
            with(moviesCache) {
                clear()
                putAll(movies.associateBy { it.ids.trakt })
            }
        }
    }

    override suspend fun getMovies(): List<Movie> {
        return mutex.withLock {
            moviesCache.values.toList()
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            moviesCache.clear()
        }
    }
}
