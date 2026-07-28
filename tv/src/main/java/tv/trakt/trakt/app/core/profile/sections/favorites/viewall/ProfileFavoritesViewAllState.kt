package tv.trakt.trakt.app.core.profile.sections.favorites.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem

@Immutable
internal data class ProfileFavoritesViewAllState(
    val isLoading: Boolean = false,
    val items: ImmutableList<FavoriteItem>? = null,
    val error: Exception? = null,
)
