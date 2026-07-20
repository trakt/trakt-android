package tv.trakt.trakt.app.core.shows.features.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.shows.model.TrendingShow
import tv.trakt.trakt.common.core.user.UserCollectionState

@Immutable
internal data class ShowsTrendingViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val shows: ImmutableList<TrendingShow>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
