package tv.trakt.trakt.core.discover.sections.anticipated

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter

@Immutable
internal data class DiscoverAnticipatedState(
    val items: ImmutableList<DiscoverItem>? = null,
    val filter: GlobalFilter? = null,
    val collapsed: Boolean? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
