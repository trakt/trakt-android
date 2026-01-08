package tv.trakt.trakt.core.summary.movies.features.related

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class MovieRelatedState(
    val items: ImmutableList<Movie>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val loading: LoadingState = LoadingState.IDLE,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
