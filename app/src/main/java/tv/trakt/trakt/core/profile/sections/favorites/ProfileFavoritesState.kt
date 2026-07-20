package tv.trakt.trakt.core.profile.sections.favorites

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ProfileFavoritesState(
    val user: User? = null,
    val items: ImmutableList<FavoriteItem>? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
