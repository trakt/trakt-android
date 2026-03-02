package tv.trakt.trakt.app.core.home.sections.startwatching

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem

@Immutable
internal data class HomeWatchlistState(
    val items: ImmutableList<WatchlistItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
