package tv.trakt.trakt.app.core.profile.sections.favorites.shows.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class ProfileFavoriteShowsViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<Show>? = null,
    val error: Exception? = null,
)
