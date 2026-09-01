package tv.trakt.trakt.core.home.sections.recommended.local.movies

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem

internal interface RecommendedMoviesLocalDataSource {
    suspend fun setMovies(movies: List<RecommendedItem.MovieItem>)

    suspend fun getMovies(): List<RecommendedItem.MovieItem>

    suspend fun removeMovie(id: TraktId)

    suspend fun clear()
}
