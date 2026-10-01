package tv.trakt.trakt.core.ratings.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface RatingsUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(): Flow<Pair<Source, Instant?>>

    enum class Source {
        Default,
        MediaDetails,
        SeasonDetails,
    }
}
