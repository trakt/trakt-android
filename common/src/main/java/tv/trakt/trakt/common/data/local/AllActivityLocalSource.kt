package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import java.time.Instant

class AllActivityLocalSource {
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun notifyUpdate() {
        updatedAt.tryEmit(nowUtcInstant())
    }

    fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
    }
}
