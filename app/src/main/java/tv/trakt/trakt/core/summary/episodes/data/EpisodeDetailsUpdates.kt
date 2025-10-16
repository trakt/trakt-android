package tv.trakt.trakt.core.summary.episodes.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface EpisodeDetailsUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant?>

    enum class Source {
        PROGRESS,
        SEASON,
    }
}
