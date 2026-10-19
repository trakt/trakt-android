package tv.trakt.trakt.common.model.calendar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.time.ZoneOffset.UTC

@Immutable
@Serializable
sealed class CalendarItem {
    abstract val watched: Boolean

    @Immutable
    @Serializable
    data class MovieItem(
        val movie: Movie,
        override val watched: Boolean = false,
    ) : CalendarItem()

    @Immutable
    @Serializable
    data class EpisodeItem(
        val show: Show,
        @Serializable(ImmutableListSerializer::class)
        val episodes: ImmutableList<Episode>,
        val isFullSeason: Boolean = false,
        override val watched: Boolean = false,
    ) : CalendarItem() {
        val episode: Episode
            get() = episodes.first()
    }

    val id: TraktId
        get() = when (this) {
            is MovieItem -> movie.ids.trakt
            is EpisodeItem -> episode.ids.trakt
        }

    val showId: TraktId?
        get() = when (this) {
            is MovieItem -> null
            is EpisodeItem -> show.ids.trakt
        }

    val key: String
        get() = when (this) {
            is MovieItem -> "${id.value}-movie"
            is EpisodeItem -> "${id.value}-episode"
        }

    val title: String
        get() = when (this) {
            is MovieItem -> movie.title
            is EpisodeItem -> episode.title
        }

    val releasedAt: Instant?
        get() = when (this) {
            is MovieItem -> movie.released?.atStartOfDay(UTC)?.toInstant()
            is EpisodeItem -> episode.releasedAt
        }

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
        }
}
