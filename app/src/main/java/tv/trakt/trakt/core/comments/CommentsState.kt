package tv.trakt.trakt.core.comments

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.model.CommentsFilter

@Immutable
internal data class CommentsState(
    val backgroundUrl: String? = null,
    val media: MediaState? = null,
    val items: ImmutableList<Comment>? = null,
    val filter: CommentsFilter = CommentsFilter.POPULAR,
    val reactions: ImmutableMap<Int, ReactionsSummary>? = null,
    val userReactions: ImmutableMap<Int, Reaction?>? = null,
    val user: User? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
) {
    data class MediaState(
        val id: TraktId,
        val type: MediaType,
    )
}
