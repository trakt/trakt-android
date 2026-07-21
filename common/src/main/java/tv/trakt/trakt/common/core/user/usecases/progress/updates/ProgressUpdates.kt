package tv.trakt.trakt.common.core.user.usecases.progress.updates

import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface ProgressUpdates {
    suspend fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
