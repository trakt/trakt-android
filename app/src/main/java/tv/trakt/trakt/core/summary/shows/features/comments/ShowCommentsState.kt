package tv.trakt.trakt.core.summary.shows.features.comments

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.model.CommentsFilter

@Immutable
internal data class ShowCommentsState(
    val items: ImmutableList<Comment>? = null,
    val filter: CommentsFilter = CommentsFilter.POPULAR,
    val reactions: ImmutableMap<Int, ReactionsSummary>? = null,
    val userReactions: ImmutableMap<Int, Reaction?>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val user: User? = null,
    val error: Exception? = null,
)
