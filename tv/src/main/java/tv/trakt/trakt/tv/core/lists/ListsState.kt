package tv.trakt.trakt.tv.core.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.tv.core.movies.model.Movie

@Immutable
internal data class ListsState(
    val watchlistMovies: ImmutableList<Movie>? = null,
    val watchlistShows: ImmutableList<Show>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
