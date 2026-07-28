package tv.trakt.trakt.app.core.profile.sections.favorites

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem

@Immutable
internal data class ProfileFavoritesState(
    val items: ImmutableList<FavoriteItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
