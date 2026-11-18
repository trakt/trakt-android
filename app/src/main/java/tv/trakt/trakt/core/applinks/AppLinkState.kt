package tv.trakt.trakt.core.applinks

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList

@Immutable
internal data class AppLinkState(
    val loading: LoadingState = LoadingState.Idle,
)

internal sealed interface AppLinkEvent {
    data class OpenShow(
        val showId: TraktId,
        val comment: Comment? = null,
    ) : AppLinkEvent

    data class OpenMovie(
        val movieId: TraktId,
        val comment: Comment? = null,
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

    data class OpenComment(
        val comment: Comment,
    ) : AppLinkEvent

    data class OpenUser(
        val user: User,
    ) : AppLinkEvent

    data class OpenList(
        val list: CustomList,
    ) : AppLinkEvent

    data object NotFound : AppLinkEvent

    data object Error : AppLinkEvent
}
