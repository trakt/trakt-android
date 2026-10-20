package tv.trakt.trakt.app.core.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.discover.DiscoverItem

@Immutable
internal data class MoviesState(
    val isLoading: Boolean = true,
    val trendingMovies: ImmutableList<DiscoverItem.MovieItem>? = null,
    val popularMovies: ImmutableList<Movie>? = null,
    val anticipatedMovies: ImmutableList<DiscoverItem.MovieItem>? = null,
    val releasesMovies: ImmutableList<CalendarItem.MovieItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val user: User? = null,
    val error: Exception? = null,
)
