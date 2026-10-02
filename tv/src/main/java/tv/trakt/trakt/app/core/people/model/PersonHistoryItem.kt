package tv.trakt.trakt.app.core.people.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import java.time.Instant
import java.time.ZoneOffset.UTC

@Immutable
internal sealed interface PersonHistoryItem {
    @Immutable
    data class ShowItem(
        val show: Show,
    ) : PersonHistoryItem

    @Immutable
    data class MovieItem(
        val movie: Movie,
    ) : PersonHistoryItem

    val key: String
        get() = when (this) {
            is ShowItem -> "${show.ids.trakt.value}-show"
            is MovieItem -> "${movie.ids.trakt.value}-movie"
        }

    val released: Instant?
        get() = when (this) {
            is ShowItem -> show.releasedAt
            is MovieItem -> movie.released?.atStartOfDay(UTC)?.toInstant()
        }
}
