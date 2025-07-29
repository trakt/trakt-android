package tv.trakt.trakt.core.shows.sections.hot

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.shows.model.WatchersShow

@Immutable
internal data class ShowsHotState(
    val items: ImmutableList<WatchersShow>? = null,
    val loading: Boolean = true,
    val error: Exception? = null,
)
