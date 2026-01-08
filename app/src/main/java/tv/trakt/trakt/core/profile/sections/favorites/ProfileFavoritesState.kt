package tv.trakt.trakt.core.profile.sections.favorites

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class ProfileFavoritesState(
    val user: User? = null,
    val items: ImmutableList<FavoriteItem>? = null,
    val filter: MediaMode? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
