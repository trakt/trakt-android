package tv.trakt.trakt.app.core.movies.features.releases

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.calendar.CalendarItem

@Immutable
internal data class MoviesReleasesViewAllState(
    val isLoading: Boolean = false,
    val items: ImmutableList<CalendarItem.MovieItem>? = null,
    val error: Exception? = null,
)
