package tv.trakt.trakt.core.lists.features.reorder.watchlist

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Images

@Immutable
internal data class WatchlistReorderState(
    val items: ImmutableList<WatchlistReorderEntry>? = null,
    val initialItemsOrder: ImmutableList<Int>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
    val done: Boolean = false,
)

@Immutable
internal data class WatchlistReorderEntry(
    val id: Int,
    val key: String,
    val title: String,
    val year: Int?,
    val images: Images?,
)
