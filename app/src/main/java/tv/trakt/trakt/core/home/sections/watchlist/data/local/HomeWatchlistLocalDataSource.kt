package tv.trakt.trakt.core.home.sections.watchlist.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import java.time.Instant

internal interface HomeWatchlistLocalDataSource {
    suspend fun addItems(
        items: List<WatchlistItem>,
        notify: Boolean,
    )

    suspend fun setItems(
        items: List<WatchlistItem>,
        notify: Boolean,
    )

    suspend fun removeItems(
        itemsKeys: List<String>,
        notify: Boolean,
    )

    suspend fun getItems(): List<WatchlistItem>

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
