package tv.trakt.trakt.core.calendar.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.time.ZoneOffset

@Immutable
internal sealed class CalendarItem(
    open val watched: Boolean,
) {
    @Immutable
    internal data class MovieItem(
        override val watched: Boolean,
        val movie: Movie,
    ) : CalendarItem(watched)

    @Immutable
    internal data class EpisodeItem(
        override val watched: Boolean,
        val episode: Episode,
        val show: Show,
        val isFullSeason: Boolean = false,
    ) : CalendarItem(watched)

    val id: TraktId
        get() = when (this) {
            is MovieItem -> movie.ids.trakt
            is EpisodeItem -> episode.ids.trakt
        }

    val title: String
        get() = when (this) {
            is MovieItem -> movie.title
            is EpisodeItem -> episode.title
        }

    val releasedAt: Instant?
        get() = when (this) {
            is MovieItem -> movie.released?.atStartOfDay(ZoneOffset.UTC)?.toInstant()
            is EpisodeItem -> episode.firstAired?.toInstant()
        }

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
        }
}
