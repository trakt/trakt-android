package tv.trakt.trakt.app.core.discover.features.releases

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.calendar.CalendarItem

@Immutable
internal data class DiscoverReleasesViewAllState(
    val isLoading: Boolean = false,
    val items: ImmutableList<CalendarItem>? = null,
    val error: Exception? = null,
)
