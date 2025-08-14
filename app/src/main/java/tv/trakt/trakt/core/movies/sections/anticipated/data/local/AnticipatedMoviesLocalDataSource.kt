package tv.trakt.trakt.core.movies.sections.anticipated.data.local

import tv.trakt.trakt.core.movies.model.WatchersMovie
import java.time.Instant

internal interface AnticipatedMoviesLocalDataSource {
    suspend fun addMovies(
        movies: List<WatchersMovie>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getMovies(): List<WatchersMovie>
}
