package tv.trakt.trakt.core.comments.features.postreply

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User

@Immutable
internal data class PostReplyState(
    val loading: LoadingState = LoadingState.IDLE,
    val user: User? = null,
    val commentUser: User? = null,
    val result: Comment? = null,
    val error: Exception? = null,
)
