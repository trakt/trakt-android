package tv.trakt.trakt.core.discover.sections.releases

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem

@Immutable
internal data class DiscoverReleasesState(
    val items: ImmutableList<CalendarItem>? = null,
    val filter: GlobalFilter? = null,
    val collapsed: Boolean? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
