package tv.trakt.trakt.core.discover.sections.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.model.DiscoverItem

@Immutable
internal data class DiscoverTrendingState(
    val items: ImmutableList<DiscoverItem>? = null,
    val filter: GlobalFilter? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
