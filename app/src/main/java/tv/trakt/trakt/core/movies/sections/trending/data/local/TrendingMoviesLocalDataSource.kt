package tv.trakt.trakt.core.movies.sections.trending.data.local

import tv.trakt.trakt.core.movies.model.WatchersMovie
import java.time.Instant

internal interface TrendingMoviesLocalDataSource {
    suspend fun addMovies(
        movies: List<WatchersMovie>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getMovies(): List<WatchersMovie>
}
