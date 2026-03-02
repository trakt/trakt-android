package tv.trakt.trakt.app.core.home.sections.shows.upcoming.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

@Immutable
internal sealed interface HomeUpcomingItem {
    @Immutable
    data class MovieItem(
        val movie: Movie,
    ) : HomeUpcomingItem

    @Immutable
    data class EpisodeItem(
        val show: Show,
        val episode: Episode,
        val isFullSeason: Boolean,
    ) : HomeUpcomingItem

    val id: Int
        get() = when (this) {
            is MovieItem -> movie.ids.trakt.value
            is EpisodeItem -> episode.ids.trakt.value
        }

    val key: String
        get() = when (this) {
            is MovieItem -> "${movie.ids.trakt.value}-movie-upc"
            is EpisodeItem -> "${episode.ids.trakt.value}-show-upc"
        }

    val title: String
        get() = when (this) {
            is MovieItem -> movie.title
            is EpisodeItem -> show.title
        }

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
        }

    val releaseAt: ZonedDateTime?
        get() = when (this) {
            is MovieItem -> movie.released?.atStartOfDay(UTC)
            is EpisodeItem -> episode.firstAired
        }
}
