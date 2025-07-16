package tv.trakt.app.tv.core.home.sections.shows.upcoming

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.core.home.sections.shows.upcoming.model.CalendarShow

@Immutable
internal data class HomeUpcomingState(
    val items: ImmutableList<CalendarShow>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
