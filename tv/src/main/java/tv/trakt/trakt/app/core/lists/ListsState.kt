package tv.trakt.trakt.app.core.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.lists.CustomList

@Immutable
internal data class ListsState(
    val watchlistMovies: ImmutableList<Movie>? = null,
    val watchlistShows: ImmutableList<Show>? = null,
    val personalLists: ImmutableList<CustomList>? = null,
    val likedLists: ImmutableList<CustomList>? = null,
    val loadingLists: LoadingState = LoadingState(),
    val error: Exception? = null,
) {
    internal data class LoadingState(
        val loadingWatchlist: Boolean = true,
        val loadingPersonal: Boolean = true,
        val loadingLiked: Boolean = true,
    )
}
