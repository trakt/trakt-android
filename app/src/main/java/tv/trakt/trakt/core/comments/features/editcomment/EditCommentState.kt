package tv.trakt.trakt.core.comments.features.editcomment

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.core.comments.model.CommentMention

@Immutable
internal data class EditCommentState(
    val loading: LoadingState = LoadingState.Idle,
    val result: Comment? = null,
    val error: Exception? = null,
    val mentions: ImmutableList<CommentMention> = persistentListOf(),
)
