package tv.trakt.trakt.app.core.details.comments

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Comment

@Immutable
internal data class CommentDetailsState(
    val isLoading: Boolean = false,
    val commentReplies: ImmutableList<Comment>? = null,
)
