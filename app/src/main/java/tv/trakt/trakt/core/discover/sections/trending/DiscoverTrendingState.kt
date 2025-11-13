package tv.trakt.trakt.core.discover.sections.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class DiscoverTrendingState(
    val items: ImmutableList<DiscoverItem>? = null,
    val watchedItems: ImmutableSet<String> = EmptyImmutableSet,
    val watchlistItems: ImmutableSet<String> = EmptyImmutableSet,
    val mode: MediaMode? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
