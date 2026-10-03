package tv.trakt.trakt.core.reactions.media

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsSummary

@Immutable
internal data class MediaReactionsState(
    val user: User? = null,
    val summary: MediaReactionsSummary = MediaReactionsSummary(),
    val userReactions: ImmutableList<MediaReaction> = persistentListOf(),
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
) {
    val isLimitReached: Boolean
        get() = userReactions.size >= MediaReaction.MAX_PER_MEDIA
}
