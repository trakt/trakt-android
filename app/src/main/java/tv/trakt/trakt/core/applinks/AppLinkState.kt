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

    data class OpenEpisode(
        val showId: TraktId,
        val episodeId: TraktId,
        val season: Int,
        val number: Int,
    ) : AppLinkEvent

    data class OpenPerson(
        val personId: TraktId,
    ) : AppLinkEvent

    data object NotFound : AppLinkEvent

    data object Error : AppLinkEvent
}
