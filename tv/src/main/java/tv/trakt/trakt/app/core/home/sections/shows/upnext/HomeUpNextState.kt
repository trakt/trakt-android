package tv.trakt.trakt.app.core.home.sections.shows.upnext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressItem

@Immutable
internal data class HomeUpNextState(
    val items: ImmutableList<ProgressItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
