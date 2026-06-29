package tv.trakt.trakt.core.userprofile.sections.favorites.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class AllUserProfileFavoritesState(
    val items: ImmutableList<FavoriteItem>? = null,
    val filter: MediaMode = MediaMode.Media,
    val sorting: Sorting = Sorting.RecentlyAdded,
    val collection: UserCollectionState = UserCollectionState.Default,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
