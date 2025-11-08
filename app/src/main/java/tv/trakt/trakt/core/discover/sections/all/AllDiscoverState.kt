package tv.trakt.trakt.core.discover.sections.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class AllDiscoverState(
    val items: ImmutableList<DiscoverItem>? = null,
    val mode: MediaMode? = null,
    val type: DiscoverSection? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingMore: LoadingState = LoadingState.IDLE,
    val backgroundUrl: String? = null,
    val error: Exception? = null,
)
