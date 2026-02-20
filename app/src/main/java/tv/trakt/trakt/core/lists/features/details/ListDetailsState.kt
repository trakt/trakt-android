package tv.trakt.trakt.core.lists.features.details

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class ListDetailsState(
    val list: ListDetailsInfo? = null,
    val liked: LikedInfo? = null,
    val items: ImmutableList<CustomListItem>? = null,
    val filter: MediaMode? = null,
    val sorting: Sorting = Sorting.Default,
    val collection: UserCollectionState = UserCollectionState.Default,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingMore: LoadingState = LoadingState.IDLE,
    val info: StringResource? = null,
    val error: Exception? = null,
) {
    @Immutable
    data class ListDetailsInfo(
        val mediaId: TraktId,
        val name: String,
        val description: String?,
    )

    @Immutable
    data class LikedInfo(
        val liked: Boolean = false,
        val loading: Boolean = false,
    )
}
