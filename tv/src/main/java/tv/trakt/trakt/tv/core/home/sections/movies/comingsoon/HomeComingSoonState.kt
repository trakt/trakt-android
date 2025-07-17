package tv.trakt.trakt.tv.core.home.sections.movies.comingsoon

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.tv.core.home.sections.movies.availablenow.model.WatchlistMovie

@Immutable
internal data class HomeComingSoonState(
    val movies: ImmutableList<WatchlistMovie>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
