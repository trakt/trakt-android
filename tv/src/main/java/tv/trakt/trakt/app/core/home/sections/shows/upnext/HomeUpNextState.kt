package tv.trakt.trakt.app.core.home.sections.shows.upnext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressShow

@Immutable
internal data class HomeUpNextState(
    val items: ImmutableList<ProgressShow>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
