package tv.trakt.trakt.common.core.home.watchlist.data

import tv.trakt.trakt.common.core.lists.model.WatchlistItem

interface HomeWatchlistLocalDataSource {
    suspend fun setShowItems(items: List<WatchlistItem.ShowItem>)

    suspend fun setMovieItems(items: List<WatchlistItem.MovieItem>)

    suspend fun getShowItems(): List<WatchlistItem.ShowItem>

    suspend fun getMovieItems(): List<WatchlistItem.MovieItem>

    fun clear()
}
