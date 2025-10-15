package tv.trakt.trakt.core.summary.shows.features.seasons.data.local

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface ShowSeasonsLocalDataSource {
    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
