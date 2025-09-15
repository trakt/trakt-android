package tv.trakt.trakt.core.lists.sections.watchlist.data.local

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

internal interface ListsWatchlistLocalDataSource {
    suspend fun addItems(items: List<WatchlistItem>)

    suspend fun getItems(): List<WatchlistItem>

    suspend fun deleteItems(ids: Set<TraktId>)

    fun clear()
}
