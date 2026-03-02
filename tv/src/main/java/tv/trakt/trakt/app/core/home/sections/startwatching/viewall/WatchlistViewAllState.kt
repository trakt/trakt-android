package tv.trakt.trakt.app.core.home.sections.startwatching.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem

@Immutable
internal data class WatchlistViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<WatchlistItem>? = null,
    val error: Exception? = null,
)
