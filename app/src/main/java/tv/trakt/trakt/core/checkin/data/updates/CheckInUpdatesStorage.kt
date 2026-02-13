package tv.trakt.trakt.core.checkin.data.updates

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates.Source
import java.time.Instant

internal class CheckInUpdatesStorage : CheckInUpdates {
    private val updatedAt = MutableSharedFlow<Pair<Source, Instant?>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun notifyUpdate(source: Source) {
        updatedAt.tryEmit(source to nowUtcInstant())
    }

    override fun observeUpdates(): Flow<Pair<Source, Instant?>> {
        return updatedAt.asSharedFlow()
    }
}
