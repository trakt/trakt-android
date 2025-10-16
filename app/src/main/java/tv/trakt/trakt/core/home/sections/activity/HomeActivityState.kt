package tv.trakt.trakt.core.home.sections.activity

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

@Immutable
internal data class HomeActivityState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val filter: HomeActivityFilter? = null,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val user: User? = null,
    val error: Exception? = null,
)
