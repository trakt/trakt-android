package tv.trakt.trakt.core.comments.features.deletecomment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class DeleteCommentState(
    val loading: LoadingState = LoadingState.IDLE,
    val user: User? = null,
    val deleted: Boolean = false,
    val error: Exception? = null,
)
