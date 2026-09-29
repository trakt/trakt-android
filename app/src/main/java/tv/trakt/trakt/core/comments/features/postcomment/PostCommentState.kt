package tv.trakt.trakt.core.comments.features.postcomment

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.comments.model.CommentMention

@Immutable
internal data class PostCommentState(
    val loading: LoadingState = LoadingState.Idle,
    val user: User? = null,
    val result: Comment? = null,
    val error: Exception? = null,
    val mentions: ImmutableList<CommentMention> = persistentListOf(),
)
