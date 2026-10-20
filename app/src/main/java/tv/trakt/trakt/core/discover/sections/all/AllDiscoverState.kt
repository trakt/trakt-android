package tv.trakt.trakt.core.discover.sections.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.discover.DiscoverSection
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter

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

internal sealed interface AllDiscoverEvent {
    data object HideError : AllDiscoverEvent
}
