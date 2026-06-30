package tv.trakt.trakt.core.user.usecases.social.updates

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface SocialUpdates {
    suspend fun notifyUpdate()

    fun observeUpdates(): Flow<Instant?>
}
