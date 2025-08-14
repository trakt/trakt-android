package tv.trakt.trakt.core.movies.sections.popular.data.local

import tv.trakt.trakt.common.model.Movie
import java.time.Instant

internal interface PopularMoviesLocalDataSource {
    suspend fun addMovies(
        movies: List<Movie>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getMovies(): List<Movie>
}
