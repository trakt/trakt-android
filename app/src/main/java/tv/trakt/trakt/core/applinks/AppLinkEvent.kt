package tv.trakt.trakt.core.applinks

import tv.trakt.trakt.common.model.TraktId

internal sealed interface AppLinkEvent {
    data class OpenShow(
        val showId: TraktId,
    ) : AppLinkEvent

    data class OpenMovie(
        val movieId: TraktId,
    ) : AppLinkEvent

    data object Error : AppLinkEvent
}
