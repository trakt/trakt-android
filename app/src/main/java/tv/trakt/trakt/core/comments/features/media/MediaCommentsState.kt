package tv.trakt.trakt.core.comments.features.media

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.model.CommentsMedia

@Immutable
internal data class MediaCommentsState(
    val media: CommentsMedia? = null,
    val items: ImmutableList<Comment>? = null,
    val filter: CommentsFilter = CommentsFilter.Popular,
    val language: String? = null,
    val reactions: ImmutableMap<Int, ReactionsSummary>? = null,
    val userReactions: ImmutableMap<Int, Reaction?>? = null,
    val translations: CommentTranslations = CommentTranslations(),
    val loading: LoadingState = LoadingState.Idle,
    val user: User? = null,
    val error: Exception? = null,
    val collapsed: Boolean? = null,
) {
    val movie: Movie?
        get() = (media as? CommentsMedia.ForMovie)?.movie

    val show: Show?
        get() = when (media) {
            is CommentsMedia.ForShow -> media.show
            is CommentsMedia.ForEpisode -> media.show
            is CommentsMedia.ForMovie, null -> null
        }

    val episode: Episode?
        get() = (media as? CommentsMedia.ForEpisode)?.episode
}
