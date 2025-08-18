package tv.trakt.trakt.app.core.movies.features.trending

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.movies.model.TrendingMovie

@Immutable
internal data class MoviesTrendingViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val movies: ImmutableList<TrendingMovie>? = null,
    val error: Exception? = null,
)
