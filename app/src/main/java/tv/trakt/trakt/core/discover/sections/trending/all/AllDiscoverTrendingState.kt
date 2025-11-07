package tv.trakt.trakt.core.discover.sections.trending.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class AllDiscoverTrendingState(
    val items: ImmutableList<DiscoverItem>? = null,
    val mode: MediaMode? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingMore: LoadingState = LoadingState.IDLE,
    val backgroundUrl: String? = null,
    val error: Exception? = null,
)
