package tv.trakt.trakt.app.core.home.sections.shows.upcoming

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem

@Immutable
internal data class HomeUpcomingState(
    val items: ImmutableList<HomeUpcomingItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
