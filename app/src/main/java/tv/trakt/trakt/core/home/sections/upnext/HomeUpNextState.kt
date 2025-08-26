package tv.trakt.trakt.core.home.sections.upnext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow

@Immutable
internal data class HomeUpNextState(
    val items: ImmutableList<ProgressShow>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
