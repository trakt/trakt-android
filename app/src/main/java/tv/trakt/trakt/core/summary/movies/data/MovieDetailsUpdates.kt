package tv.trakt.trakt.core.summary.movies.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface MovieDetailsUpdates {
    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
