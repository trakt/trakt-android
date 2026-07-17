package tv.trakt.trakt.core.home.sections.recommended

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.model.DiscoverItem

@Immutable
internal data class HomeRecommendedState(
    val items: ImmutableList<DiscoverItem>? = null,
    val filter: GlobalFilter? = null,
    val collapsed: Boolean? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
