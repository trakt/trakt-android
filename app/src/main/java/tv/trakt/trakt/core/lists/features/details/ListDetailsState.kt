package tv.trakt.trakt.core.lists.features.details

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.CustomListItem

@Immutable
internal data class ListDetailsState(
    val list: ListDetails? = null,
    val liked: LikedInfo? = null,
    val items: ImmutableList<CustomListItem>? = null,
    val filter: GlobalFilter? = null,
    val sorting: Sorting = Sorting.Default,
    val collection: UserCollectionState = UserCollectionState.Default,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val user: User? = null,
    val info: StringResource? = null,
    val error: Exception? = null,
) {
    @Immutable
    data class ListDetails(
        val list: CustomList,
        val mediaId: TraktId,
    )

    @Immutable
    data class LikedInfo(
        val liked: Boolean = false,
        val loading: Boolean = false,
    )
}
