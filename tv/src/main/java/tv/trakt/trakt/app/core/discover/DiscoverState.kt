package tv.trakt.trakt.app.core.discover

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.discover.DiscoverItem

@Immutable
internal data class DiscoverState(
    val mode: MediaMode = MediaMode.Media,
    val isLoading: Boolean = true,
    val trending: ImmutableList<DiscoverItem>? = null,
    val releases: ImmutableList<CalendarItem>? = null,
    val anticipated: ImmutableList<DiscoverItem>? = null,
    val popular: ImmutableList<DiscoverItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
