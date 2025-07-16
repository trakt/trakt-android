package tv.trakt.app.tv.core.profile.sections.favorites.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.core.shows.model.Show

@Immutable
internal data class ProfileFavoriteShowsState(
    val items: ImmutableList<Show>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
