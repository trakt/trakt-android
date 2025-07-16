package tv.trakt.app.tv.core.details.comments

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.common.model.Comment

@Immutable
internal data class CommentDetailsState(
    val isLoading: Boolean = false,
    val commentReplies: ImmutableList<Comment>? = null,
)
