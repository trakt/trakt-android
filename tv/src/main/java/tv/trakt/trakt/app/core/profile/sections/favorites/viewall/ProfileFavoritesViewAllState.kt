package tv.trakt.trakt.app.core.profile.sections.favorites.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem
import tv.trakt.trakt.common.core.user.UserCollectionState

@Immutable
internal data class ProfileFavoritesViewAllState(
    val isLoading: Boolean = false,
    val items: ImmutableList<FavoriteItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
