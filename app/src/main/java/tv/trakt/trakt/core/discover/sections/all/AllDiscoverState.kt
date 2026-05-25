package tv.trakt.trakt.core.discover.sections.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class AllDiscoverState(
    val user: User? = null,
    val items: ImmutableList<DiscoverItem>? = null,
    val filter: GlobalFilter? = null,
    val type: DiscoverSection? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
