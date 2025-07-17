package tv.trakt.trakt.tv.core.movies.data.local

import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.movies.model.Movie
import java.util.concurrent.ConcurrentHashMap

// TODO Temporary cache implementation, replace with a proper database solution later.
internal class MovieStorage : MovieLocalDataSource {
    private val cache = ConcurrentHashMap<TraktId, Movie>(persistentMapOf())

    override suspend fun getMovie(movieId: TraktId): Movie? {
        return cache[movieId]
    }

    override suspend fun upsertMovies(movies: List<Movie>) {
        val map = movies.associateBy { it.ids.trakt }
        cache.putAll(map)
    }
}
