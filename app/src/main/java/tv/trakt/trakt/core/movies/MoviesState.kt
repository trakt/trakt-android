package tv.trakt.trakt.core.movies

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class MoviesState(
    val backgroundUrl: String? = null,
    val user: UserState = UserState(),
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.IDLE,
    )
}
