package tv.trakt.trakt.core.profile.sections.favorites

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.lists.model.ListsMediaFilter
import tv.trakt.trakt.core.profile.model.FavoriteItem

@Immutable
internal data class ProfileFavoritesState(
    val user: User? = null,
    val items: ImmutableList<FavoriteItem>? = null,
    val filter: ListsMediaFilter = ListsMediaFilter.MEDIA,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
