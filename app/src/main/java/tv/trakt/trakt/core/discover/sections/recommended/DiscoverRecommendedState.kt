package tv.trakt.trakt.core.discover.sections.recommended

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class DiscoverRecommendedState(
    val items: ImmutableList<DiscoverItem>? = null,
    val mode: MediaMode? = null,
    val collapsed: Boolean? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
