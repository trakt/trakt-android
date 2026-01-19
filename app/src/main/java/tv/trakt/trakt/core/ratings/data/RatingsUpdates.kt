package tv.trakt.trakt.core.ratings.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface RatingsUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant?>

    enum class Source {
        POST_RATING,
    }
}
