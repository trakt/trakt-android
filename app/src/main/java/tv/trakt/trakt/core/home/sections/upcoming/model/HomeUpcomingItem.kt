package tv.trakt.trakt.core.home.sections.upcoming.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.episodes.model.Episode
import java.time.Instant

@Immutable
internal sealed class HomeUpcomingItem(
    open val id: TraktId,
    open val releasedAt: Instant,
) {
    @Immutable
    internal data class MovieItem(
        override val id: TraktId,
        override val releasedAt: Instant,
        val movie: Movie,
    ) : HomeUpcomingItem(id, releasedAt)

    @Immutable
    internal data class EpisodeItem(
        override val id: TraktId,
        override val releasedAt: Instant,
        val episode: Episode,
        val show: Show,
        val isFullSeason: Boolean = false,
    ) : HomeUpcomingItem(id, releasedAt)

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
        }
}
