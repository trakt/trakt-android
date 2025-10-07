package tv.trakt.trakt.core.comments.details

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User

@Immutable
internal data class CommentDetailsState(
    val comment: Comment? = null,
    val replies: ImmutableList<Comment>? = null,
    val user: User? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
