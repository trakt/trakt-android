package tv.trakt.trakt.core.home.sections.recommended.local.movies

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface RecommendedMoviesLocalDataSource {
    suspend fun setMovies(movies: List<DiscoverItem.MovieItem>)

    suspend fun getMovies(): List<DiscoverItem.MovieItem>

    suspend fun removeMovie(id: TraktId)

    suspend fun clear()
}
