package tv.trakt.trakt.core.profile.sections.activity.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import java.time.Instant

@Immutable
internal sealed interface ProfileCommentItem {
    val comment: Comment

    @Immutable
    data class ShowItem(
        val show: Show,
        override val comment: Comment,
    ) : ProfileCommentItem

    @Immutable
    data class MovieItem(
        val movie: Movie,
        override val comment: Comment,
    ) : ProfileCommentItem

    @Immutable
    data class EpisodeItem(
        val show: Show,
        val episode: Episode,
        override val comment: Comment,
    ) : ProfileCommentItem

    val key: String
        get() = when (this) {
            is ShowItem -> "show-${show.ids.trakt.value}"
            is MovieItem -> "movie-${movie.ids.trakt.value}"
            is EpisodeItem -> "episode-${show.ids.trakt.value}-${episode.ids.trakt.value}"
        }

    val title: String
        @Composable
        get() = when (this) {
            is ShowItem -> show.title
            is MovieItem -> movie.title
            is EpisodeItem -> "${show.title} - ${episode.seasonEpisode.toDisplayString()}"
        }

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
            is EpisodeItem -> episode.images ?: show.images
        }

    val commentedAt: Instant
        get() = comment.createdAt.toInstant()
}
