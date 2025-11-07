package tv.trakt.trakt.core.discover.sections.recommended.data.local.movies

import tv.trakt.trakt.core.discover.model.DiscoverItem
import java.time.Instant

internal interface RecommendedMoviesLocalDataSource {
    suspend fun addMovies(
        movies: List<DiscoverItem.MovieItem>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getMovies(): List<DiscoverItem.MovieItem>

    suspend fun clear()
}
