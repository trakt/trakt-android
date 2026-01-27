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
    open val id: TraktId,
    open val watched: Boolean,
    open val loading: Boolean,
) {
    @Immutable
    internal data class MovieItem(
        override val id: TraktId,
        override val watched: Boolean,
        override val loading: Boolean,
        val movie: Movie,
    ) : CalendarItem(id, watched, loading)

    @Immutable
    internal data class EpisodeItem(
        override val id: TraktId,
        override val watched: Boolean,
        override val loading: Boolean,
        val episode: Episode,
        val show: Show,
        val isFullSeason: Boolean = false,
    ) : CalendarItem(id, watched, loading)

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
