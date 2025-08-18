package tv.trakt.trakt.app.core.movies.features.anticipated

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.movies.model.AnticipatedMovie

@Immutable
internal data class MoviesAnticipatedViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val movies: ImmutableList<AnticipatedMovie>? = null,
    val error: Exception? = null,
)
