package tv.trakt.app.tv.core.home.sections.movies.availablenow

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.core.home.sections.movies.availablenow.model.WatchlistMovie

@Immutable
internal data class HomeAvailableNowState(
    val movies: ImmutableList<WatchlistMovie>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
