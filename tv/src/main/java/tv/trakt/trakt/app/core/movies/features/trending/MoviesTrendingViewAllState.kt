package tv.trakt.trakt.app.core.movies.features.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.discover.DiscoverItem

@Immutable
internal data class MoviesTrendingViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val movies: ImmutableList<DiscoverItem.MovieItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
