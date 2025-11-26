package tv.trakt.trakt.core.home.sections.watchlist.data.local

import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

internal interface HomeWatchlistLocalDataSource {
    suspend fun setItems(items: List<WatchlistItem>)

    suspend fun getItems(): List<WatchlistItem>

    suspend fun getShowItems(): List<WatchlistItem.ShowItem>

    suspend fun getMovieItems(): List<WatchlistItem.MovieItem>

    fun clear()
}
