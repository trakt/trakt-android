package tv.trakt.trakt.core.comments

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.comments.model.CommentsFilter

@Immutable
internal data class CommentsState(
    val backgroundUrl: String? = null,
    val items: ImmutableList<Comment>? = null,
    val filter: CommentsFilter = CommentsFilter.POPULAR,
    val user: User? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
