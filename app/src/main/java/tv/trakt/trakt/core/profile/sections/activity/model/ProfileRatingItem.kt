package tv.trakt.trakt.core.profile.sections.activity.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import java.time.Instant

@Immutable
internal sealed interface ProfileRatingItem {
    val rating: Int
    val ratedAt: Instant

    @Immutable
    data class ShowItem(
        val show: Show,
        override val rating: Int,
        override val ratedAt: Instant,
    ) : ProfileRatingItem

    @Immutable
    data class MovieItem(
        val movie: Movie,
        override val rating: Int,
        override val ratedAt: Instant,
    ) : ProfileRatingItem

    @Immutable
    data class EpisodeItem(
        val show: Show,
        val episode: Episode,
        override val rating: Int,
        override val ratedAt: Instant,
    ) : ProfileRatingItem

    val key: String
        get() = when (this) {
            is ShowItem -> "show-${show.ids.trakt.value}"
            is MovieItem -> "movie-${movie.ids.trakt.value}"
            is EpisodeItem -> "episode-${show.ids.trakt.value}-${episode.ids.trakt.value}"
        }
}
