package tv.trakt.trakt.tv.core.movies.data.local

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.movies.model.Movie

internal interface MovieLocalDataSource {
    suspend fun getMovie(movieId: TraktId): Movie?

    suspend fun upsertMovies(movies: List<Movie>)
}
