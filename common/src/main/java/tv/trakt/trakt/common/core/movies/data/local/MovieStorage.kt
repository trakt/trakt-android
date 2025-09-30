package tv.trakt.trakt.common.core.movies.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId

class MovieStorage : MovieLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, Movie>()

    override suspend fun getMovie(movieId: TraktId): Movie? {
        return mutex.withLock {
            storage[movieId]
        }
    }

    override suspend fun upsertMovies(movies: List<Movie>) {
        mutex.withLock {
            with(storage) {
                putAll(movies.associateBy { it.ids.trakt })
            }
        }
    }
}
