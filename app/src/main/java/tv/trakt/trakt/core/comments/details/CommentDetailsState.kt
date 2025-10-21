package tv.trakt.trakt.core.comments.details

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary

@Immutable
internal data class CommentDetailsState(
    val comment: Comment? = null,
    val replies: ImmutableList<Comment>? = null,
    val reactions: ImmutableMap<Int, ReactionsSummary>? = null,
    val user: User? = null,
    val userReactions: ImmutableMap<Int, Reaction?>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
