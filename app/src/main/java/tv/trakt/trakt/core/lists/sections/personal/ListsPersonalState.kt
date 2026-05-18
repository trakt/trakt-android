package tv.trakt.trakt.core.lists.sections.personal

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class ListsPersonalState(
    val user: User? = null,
    val list: CustomList? = null,
    val items: ImmutableList<CustomListItem>? = null,
    val filter: GlobalFilter? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
