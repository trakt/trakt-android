package tv.trakt.trakt.core.comments.features.editcomment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment

@Immutable
internal data class EditCommentState(
    val loading: LoadingState = LoadingState.Idle,
    val result: Comment? = null,
    val error: Exception? = null,
)
