package tv.trakt.trakt.core.comments.features.details

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations

@Immutable
internal data class CommentDetailsState(
    val comment: Comment? = null,
    val replies: ImmutableList<Comment>? = null,
    val reactions: ImmutableMap<Int, ReactionsSummary>? = null,
    val user: User? = null,
    val userReactions: ImmutableMap<Int, Reaction?>? = null,
    val translations: CommentTranslations = CommentTranslations(),
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
