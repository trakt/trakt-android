package tv.trakt.trakt.core.checkin.data.updates

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface CheckInUpdates {
    fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
