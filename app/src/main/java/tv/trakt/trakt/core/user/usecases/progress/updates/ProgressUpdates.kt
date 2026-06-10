package tv.trakt.trakt.core.user.usecases.progress.updates

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface ProgressUpdates {
    suspend fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
