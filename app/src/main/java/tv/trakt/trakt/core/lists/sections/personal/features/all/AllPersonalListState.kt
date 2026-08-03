package tv.trakt.trakt.core.lists.sections.personal.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.CustomListItem

@Immutable
internal data class AllPersonalListState(
    val user: User? = null,
    val list: CustomList? = null,
    val filter: GlobalFilter? = null,
    val sorting: Sorting = Sorting.Default,
    val items: ImmutableList<CustomListItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
