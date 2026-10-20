package tv.trakt.trakt.app.core.shows.features.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.discover.DiscoverItem

@Immutable
internal data class ShowsTrendingViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val shows: ImmutableList<DiscoverItem.ShowItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
