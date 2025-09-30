package tv.trakt.trakt.common.core.movies.data.local

import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId

interface MovieLocalDataSource {
    suspend fun getMovie(movieId: TraktId): Movie?

    suspend fun upsertMovies(movies: List<Movie>)
}
