package tv.trakt.trakt.app.core.movies.features.recommended

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.Movie

@Immutable
internal data class MoviesRecommendedViewAllState(
    val isLoading: Boolean = false,
    val movies: ImmutableList<Movie>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
