package tv.trakt.trakt.app.core.details.lists.details.media

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.sorting.Sorting

@Immutable
internal data class CustomListMediaState(
    val items: ImmutableList<ListMediaItem>? = null,
    val like: LikedState = LikedState(),
    val collection: UserCollectionState = UserCollectionState.Default,
    val sorting: Sorting = Sorting.Default,
    val itemsSorting: Sorting = sorting,
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val info: StringResource? = null,
    val error: Exception? = null,
) {
    data class LikedState(
        val likesCount: Int = 0,
        val isLiked: Boolean = false,
        val isLoading: Boolean = false,
    )
}
