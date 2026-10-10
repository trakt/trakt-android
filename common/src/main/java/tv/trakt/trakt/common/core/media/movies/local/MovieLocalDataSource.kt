package tv.trakt.trakt.common.core.media.movies.local

import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId

interface MovieLocalDataSource {
    suspend fun getMovie(movieId: TraktId): Movie?

    suspend fun upsertMovies(movies: List<Movie>)
}
