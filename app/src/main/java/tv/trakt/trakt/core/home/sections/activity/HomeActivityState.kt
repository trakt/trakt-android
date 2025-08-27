package tv.trakt.trakt.core.home.sections.activity

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

@Immutable
internal data class HomeActivityState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val filter: HomeActivityFilter? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
