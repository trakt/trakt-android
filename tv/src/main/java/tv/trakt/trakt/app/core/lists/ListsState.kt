package tv.trakt.trakt.app.core.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList

@Immutable
internal data class ListsState(
    val mode: MediaMode = MediaMode.Media,
    val watchlist: ImmutableList<ListMediaItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val smartLists: ImmutableList<SmartList>? = null,
    val personalLists: ImmutableList<CustomList>? = null,
    val likedLists: ImmutableList<CustomList>? = null,
    val loadingLists: LoadingState = LoadingState(),
    val error: Exception? = null,
) {
    internal data class LoadingState(
        val loadingWatchlist: Boolean = true,
        val loadingSmart: Boolean = true,
        val loadingPersonal: Boolean = true,
        val loadingLiked: Boolean = true,
    )
}
