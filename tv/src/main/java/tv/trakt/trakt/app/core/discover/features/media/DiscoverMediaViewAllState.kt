package tv.trakt.trakt.app.core.discover.features.media

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.discover.model.DiscoverMediaSection
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.discover.DiscoverItem

@Immutable
internal data class DiscoverMediaViewAllState(
    val section: DiscoverMediaSection = DiscoverMediaSection.Trending,
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<DiscoverItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
