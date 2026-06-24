package tv.trakt.trakt.core.summary.movies.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface MovieDetailsUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant?>

    enum class Source {
        Progress,
        History,
    }
}
