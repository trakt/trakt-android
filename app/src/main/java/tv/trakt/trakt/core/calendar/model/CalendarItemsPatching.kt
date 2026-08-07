package tv.trakt.trakt.core.calendar.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.model.TraktId
import java.time.LocalDate

internal typealias CalendarItems = ImmutableMap<LocalDate, ImmutableList<CalendarItem>>

/** Optimistic local patches applied while a history change is in flight. */
internal fun CalendarItems.withWatched(
    id: TraktId,
    watched: Boolean,
): CalendarItems {
    return mapValues { entry ->
        entry.value.map { item ->
            when (item.id) {
                id -> when (item) {
                    is CalendarItem.EpisodeItem -> item.copy(watched = watched)
                    is CalendarItem.MovieItem -> item.copy(watched = watched)
                }

                else -> item
            }
        }.toImmutableList()
    }.toImmutableMap()
}

internal fun CalendarItems.withoutMovie(id: TraktId): CalendarItems {
    return mapValues { entry ->
        entry.value.filterNot { item ->
            item is CalendarItem.MovieItem && item.id == id
        }.toImmutableList()
    }.toImmutableMap()
}
