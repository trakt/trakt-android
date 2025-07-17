package tv.trakt.trakt.tv.core.profile.sections.favorites.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.tv.core.movies.model.Movie

@Immutable
internal data class ProfileFavoriteMoviesState(
    val items: ImmutableList<Movie>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
