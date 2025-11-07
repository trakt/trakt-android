package tv.trakt.trakt.core.discover.sections.popular.data.local.movies

import tv.trakt.trakt.core.discover.model.DiscoverItem
import java.time.Instant

internal interface PopularMoviesLocalDataSource {
    suspend fun addMovies(
        movies: List<DiscoverItem.MovieItem>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getMovies(): List<DiscoverItem.MovieItem>
}
