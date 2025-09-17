package tv.trakt.trakt.core.home.sections.watchlist.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.watchlist.model.WatchlistMovie
import java.time.Instant

internal interface HomeWatchlistLocalDataSource {
    suspend fun addItems(items: List<WatchlistMovie>)

    suspend fun getItems(): List<WatchlistMovie>

    suspend fun removeItems(ids: Set<TraktId>)

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
