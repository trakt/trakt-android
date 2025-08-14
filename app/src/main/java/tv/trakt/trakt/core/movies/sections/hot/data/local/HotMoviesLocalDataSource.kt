package tv.trakt.trakt.core.movies.sections.hot.data.local

import tv.trakt.trakt.core.movies.model.WatchersMovie
import java.time.Instant

internal interface HotMoviesLocalDataSource {
    suspend fun addMovies(
        movies: List<WatchersMovie>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getMovies(): List<WatchersMovie>
}
