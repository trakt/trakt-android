package tv.trakt.trakt.core.userprofile.sections.favorites

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class UserProfileFavoritesState(
    val items: ImmutableList<FavoriteItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
