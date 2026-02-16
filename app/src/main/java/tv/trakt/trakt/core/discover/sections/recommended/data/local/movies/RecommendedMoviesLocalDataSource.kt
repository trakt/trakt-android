package tv.trakt.trakt.core.discover.sections.recommended.data.local.movies

import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface RecommendedMoviesLocalDataSource {
    suspend fun setMovies(movies: List<DiscoverItem.MovieItem>)

    suspend fun getMovies(): List<DiscoverItem.MovieItem>

    suspend fun clear()
}
