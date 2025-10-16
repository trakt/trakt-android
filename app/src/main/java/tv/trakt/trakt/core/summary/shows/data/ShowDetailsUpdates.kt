package tv.trakt.trakt.core.summary.shows.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface ShowDetailsUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant?>

    enum class Source {
        PROGRESS,
        SEASONS,
    }
}
