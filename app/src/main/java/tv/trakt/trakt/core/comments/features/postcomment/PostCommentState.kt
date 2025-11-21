package tv.trakt.trakt.core.comments.features.postcomment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User

@Immutable
internal data class PostCommentState(
    val loading: LoadingState = LoadingState.IDLE,
    val user: User? = null,
    val result: Comment? = null,
    val error: Exception? = null,
)
