package tv.trakt.trakt.core.lists.sections.watchlist.features.all.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface AllWatchlistLocalDataSource {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant?>

    enum class Source {
        ALL,
    }
}
