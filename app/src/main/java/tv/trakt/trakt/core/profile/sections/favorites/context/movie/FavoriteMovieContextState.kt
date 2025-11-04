package tv.trakt.trakt.core.profile.sections.favorites.context.movie

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.model.User

@Immutable
internal data class FavoriteMovieContextState(
    val loading: LoadingState = IDLE,
    val user: User? = null,
    val error: Exception? = null,
)
