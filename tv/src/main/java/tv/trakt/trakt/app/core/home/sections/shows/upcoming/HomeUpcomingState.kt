package tv.trakt.trakt.app.core.home.sections.shows.upcoming

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.calendar.CalendarItem

@Immutable
internal data class HomeUpcomingState(
    val items: ImmutableList<CalendarItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
