package tv.trakt.trakt.app.core.scrobble.data.local

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface ScrobbleUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant>

    enum class Source {
        SCROBBLE_START_WORKER,
        SCROBBLE_STOP_WORKER,
    }
}
