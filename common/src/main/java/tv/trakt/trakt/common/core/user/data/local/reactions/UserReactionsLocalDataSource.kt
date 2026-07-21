package tv.trakt.trakt.common.core.user.data.local.reactions

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.reactions.Reaction
import java.time.Instant

interface UserReactionsLocalDataSource {
    suspend fun setReactions(
        reactions: Map<Int, Reaction?>,
        notify: Boolean = false,
    )

    suspend fun getReactions(): Map<Int, Reaction?>

    suspend fun isLoaded(): Boolean

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
