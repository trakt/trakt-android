package tv.trakt.trakt.core.home.sections.watchlist.data.local

import tv.trakt.trakt.core.home.sections.watchlist.model.WatchlistMovie

internal interface HomeWatchlistLocalDataSource {
    suspend fun addItems(items: List<WatchlistMovie>)

    suspend fun getItems(): List<WatchlistMovie>

    fun clear()
}
