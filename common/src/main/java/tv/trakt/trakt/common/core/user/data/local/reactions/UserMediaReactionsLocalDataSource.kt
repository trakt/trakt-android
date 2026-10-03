package tv.trakt.trakt.common.core.user.data.local.reactions

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.model.reactions.UserMediaReaction
import java.time.Instant

/**
 * In-memory cache of the user's media reactions, filled per item since the API
 * has no bulk endpoint. Cleared on logout.
 */
interface UserMediaReactionsLocalDataSource {
    suspend fun setReactions(
        target: MediaReactionsTarget,
        reactions: List<UserMediaReaction>,
        notify: Boolean = false,
    )

    suspend fun getReactions(target: MediaReactionsTarget): List<UserMediaReaction>

    suspend fun isLoaded(target: MediaReactionsTarget): Boolean

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
