package tv.trakt.trakt.core.profile.sections.favorites.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class AllFavoritesState(
    val filter: MediaMode? = null,
    val sorting: Sorting = Sorting.RecentlyAdded,
    val items: ImmutableList<FavoriteItem>? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
