package tv.trakt.trakt.core.home.sections.watchlist.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import java.time.Instant

internal interface HomeWatchlistLocalDataSource {
    suspend fun addItems(items: List<WatchlistItem>)

    suspend fun setItems(items: List<WatchlistItem>)

    suspend fun removeItems(itemsKeys: List<String>)

    suspend fun getItems(): List<WatchlistItem>

    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
