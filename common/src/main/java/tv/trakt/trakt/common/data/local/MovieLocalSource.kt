package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId

class MovieLocalSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, Movie>()

    suspend fun getMovie(movieId: TraktId): Movie? {
        return mutex.withLock {
            storage[movieId]
        }
    }

    suspend fun upsertMovies(movies: List<Movie>) {
        mutex.withLock {
            with(storage) {
                putAll(movies.associateBy { it.ids.trakt })
            }
        }
    }
}
