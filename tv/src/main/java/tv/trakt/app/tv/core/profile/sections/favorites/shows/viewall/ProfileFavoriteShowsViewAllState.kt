package tv.trakt.app.tv.core.profile.sections.favorites.shows.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.core.shows.model.Show

@Immutable
internal data class ProfileFavoriteShowsViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<Show>? = null,
    val error: Exception? = null,
)
