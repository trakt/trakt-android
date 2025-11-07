package tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies

import tv.trakt.trakt.core.discover.model.DiscoverItem
import java.time.Instant

internal interface AnticipatedMoviesLocalDataSource {
    suspend fun addMovies(
        movies: List<DiscoverItem.MovieItem>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getMovies(): List<DiscoverItem.MovieItem>
}
