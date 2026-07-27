package tv.trakt.trakt.app.core.shows.features.releases

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem

@Immutable
internal data class ShowsReleasesViewAllState(
    val isLoading: Boolean = false,
    val items: ImmutableList<HomeUpcomingItem.EpisodeItem>? = null,
    val error: Exception? = null,
)
