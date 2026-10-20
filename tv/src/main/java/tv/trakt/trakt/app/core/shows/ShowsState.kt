package tv.trakt.trakt.app.core.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.discover.DiscoverItem

@Immutable
internal data class ShowsState(
    val isLoading: Boolean = true,
    val trendingShows: ImmutableList<DiscoverItem.ShowItem>? = null,
    val popularShows: ImmutableList<Show>? = null,
    val anticipatedShows: ImmutableList<DiscoverItem.ShowItem>? = null,
    val releasesShows: ImmutableList<CalendarItem.EpisodeItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val user: User? = null,
    val error: Exception? = null,
)
