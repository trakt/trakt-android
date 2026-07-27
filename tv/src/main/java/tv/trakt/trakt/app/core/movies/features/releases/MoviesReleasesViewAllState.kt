package tv.trakt.trakt.app.core.movies.features.releases

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem

@Immutable
internal data class MoviesReleasesViewAllState(
    val isLoading: Boolean = false,
    val items: ImmutableList<HomeUpcomingItem.MovieItem>? = null,
    val error: Exception? = null,
)
