package tv.trakt.trakt.app.core.home.sections.shows.upnext.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressShow

@Immutable
internal data class UpNextViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<ProgressShow>? = null,
    val error: Exception? = null,
)
