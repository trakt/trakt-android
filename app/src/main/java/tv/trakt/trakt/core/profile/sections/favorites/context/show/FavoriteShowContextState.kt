package tv.trakt.trakt.core.profile.sections.favorites.context.show

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.User

@Immutable
internal data class FavoriteShowContextState(
    val isWatched: Boolean = false,
    val loadingFavorite: LoadingState = Idle,
    val loadingWatched: LoadingState = Idle,
    val user: User? = null,
    val error: Exception? = null,
)
