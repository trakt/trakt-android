package tv.trakt.trakt.app.core.movies.data.local

import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId

internal interface MovieLocalDataSource {
    suspend fun getMovie(movieId: TraktId): Movie?

    suspend fun upsertMovies(movies: List<Movie>)
}
