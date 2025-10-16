package tv.trakt.trakt.core.summary.episodes.features.seasons.local

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface EpisodeSeasonsLocalDataSource {
    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
