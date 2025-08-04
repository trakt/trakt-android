package tv.trakt.trakt.app.core.profile.sections.favorites.movies.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.movies.model.Movie

@Immutable
internal data class ProfileFavoriteMoviesViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<Movie>? = null,
    val error: Exception? = null,
)
