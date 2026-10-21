package tv.trakt.trakt.core.calendar.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.calendar.CalendarItem
import java.time.LocalDate

internal typealias CalendarItems = ImmutableMap<LocalDate, ImmutableList<CalendarItem>>

// Optimistic local patches applied while a history change is in flight. Episode and movie
// ids are separate namespaces, so each patch matches its own media type only.
internal fun CalendarItems.withEpisodeWatched(
    id: TraktId,
    watched: Boolean,
): CalendarItems {
    return mapItems { item ->
        when {
            item is CalendarItem.EpisodeItem && item.id == id -> item.copy(watched = watched)
            else -> item
        }
    }
}

internal fun CalendarItems.withMovieWatched(
    id: TraktId,
    watched: Boolean,
): CalendarItems {
    return mapItems { item ->
        when {
            item is CalendarItem.MovieItem && item.id == id -> item.copy(watched = watched)
            else -> item
        }
    }
}

internal fun CalendarItems.withoutMovie(id: TraktId): CalendarItems {
    return mapValues { entry ->
        entry.value.filterNot { item ->
            item is CalendarItem.MovieItem && item.id == id
        }.toImmutableList()
    }.toImmutableMap()
}

private fun CalendarItems.mapItems(transform: (CalendarItem) -> CalendarItem): CalendarItems {
    return mapValues { entry ->
        entry.value.map(transform).toImmutableList()
    }.toImmutableMap()
}
