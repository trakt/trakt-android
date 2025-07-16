package tv.trakt.app.tv.core.movies.data.local

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.movies.model.Movie

internal interface MovieLocalDataSource {
    suspend fun getMovie(movieId: TraktId): Movie?

    suspend fun upsertMovies(movies: List<Movie>)
}
