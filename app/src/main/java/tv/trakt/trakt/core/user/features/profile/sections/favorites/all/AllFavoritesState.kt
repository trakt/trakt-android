package tv.trakt.trakt.core.user.features.profile.sections.favorites.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.ListsMediaFilter
import tv.trakt.trakt.core.user.features.profile.model.FavoriteItem

@Immutable
internal data class AllFavoritesState(
    val backgroundUrl: String? = null,
    val filter: ListsMediaFilter? = null,
    val items: ImmutableList<FavoriteItem>? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
