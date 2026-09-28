package tv.trakt.trakt.core.applinks

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class AppLinkState(
    val loading: LoadingState = LoadingState.Idle,
)

internal sealed interface AppLinkEvent {
    data class OpenShow(
        val showId: TraktId,
    ) : AppLinkEvent

    data class OpenMovie(
        val movieId: TraktId,
    ) : AppLinkEvent

    data object Error : AppLinkEvent
}
