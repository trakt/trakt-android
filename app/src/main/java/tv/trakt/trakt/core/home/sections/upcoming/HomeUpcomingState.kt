package tv.trakt.trakt.core.home.sections.upcoming

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.home.sections.upcoming.model.CalendarShow

@Immutable
internal data class HomeUpcomingState(
    val items: ImmutableList<CalendarShow>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
