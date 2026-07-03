package tv.trakt.trakt.core.home.sections.upcoming.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
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
        val episodes: ImmutableList<Episode>,
        val show: Show,
        val isFullSeason: Boolean = false,
    ) : HomeUpcomingItem(id, releasedAt) {
        val episode: Episode
            get() = episodes.first()
    }

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
        }
}
