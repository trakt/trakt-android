package tv.trakt.trakt.core.home.sections.watchlist.data.local

import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

internal interface HomeWatchlistLocalDataSource {
    suspend fun setShowItems(items: List<WatchlistItem.ShowItem>)

    suspend fun setMovieItems(items: List<WatchlistItem.MovieItem>)

    suspend fun getShowItems(): List<WatchlistItem.ShowItem>

    suspend fun getMovieItems(): List<WatchlistItem.MovieItem>

    fun clear()
}
