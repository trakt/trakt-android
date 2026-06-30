package tv.trakt.trakt.core.user.usecases.social.updates

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import java.time.Instant

class SocialUpdatesStorage : SocialUpdates {
    private val updatedAt = MutableSharedFlow<Instant?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun notifyUpdate() {
        updatedAt.emit(nowUtcInstant())
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt.asSharedFlow()
    }
}
