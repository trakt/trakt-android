package tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies

import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface AnticipatedMoviesLocalDataSource {
    suspend fun setMovies(movies: List<DiscoverItem.MovieItem>)

    suspend fun getMovies(): List<DiscoverItem.MovieItem>
}
