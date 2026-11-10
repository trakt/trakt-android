package tv.trakt.trakt.core.comments.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

@Immutable
internal sealed interface CommentsMedia {
    data class ForMovie(
        val movie: Movie,
    ) : CommentsMedia

    data class ForShow(
        val show: Show,
    ) : CommentsMedia

    data class ForEpisode(
        val show: Show,
        val episode: Episode,
    ) : CommentsMedia
}
