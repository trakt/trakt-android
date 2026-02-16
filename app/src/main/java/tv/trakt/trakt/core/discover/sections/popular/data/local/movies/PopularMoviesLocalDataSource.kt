package tv.trakt.trakt.core.discover.sections.popular.data.local.movies

import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface PopularMoviesLocalDataSource {
    suspend fun setMovies(movies: List<DiscoverItem.MovieItem>)

    suspend fun getMovies(): List<DiscoverItem.MovieItem>
}
