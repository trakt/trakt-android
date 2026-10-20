package tv.trakt.trakt.app.core.shows.features.releases

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.calendar.CalendarItem

@Immutable
internal data class ShowsReleasesViewAllState(
    val isLoading: Boolean = false,
    val items: ImmutableList<CalendarItem.EpisodeItem>? = null,
    val error: Exception? = null,
)
