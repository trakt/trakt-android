package tv.trakt.trakt.core.discover.sections.trending.data.local.movies

import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface TrendingMoviesLocalDataSource {
    suspend fun setMovies(movies: List<DiscoverItem.MovieItem>)

    suspend fun getMovies(): List<DiscoverItem.MovieItem>
}
