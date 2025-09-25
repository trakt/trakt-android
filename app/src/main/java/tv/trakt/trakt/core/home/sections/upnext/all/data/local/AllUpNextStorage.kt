package tv.trakt.trakt.core.home.sections.upnext.all.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import java.time.Instant

internal class AllUpNextStorage : AllUpNextLocalDataSource {
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun notifyUpdate() {
        updatedAt.tryEmit(nowUtcInstant())
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
    }
}
