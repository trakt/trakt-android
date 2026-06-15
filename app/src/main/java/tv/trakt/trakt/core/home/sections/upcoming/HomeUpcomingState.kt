package tv.trakt.trakt.core.home.sections.upcoming

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem

@Immutable
internal data class HomeUpcomingState(
    val user: User? = null,
    val items: ImmutableList<HomeUpcomingItem>? = null,
    val filter: GlobalFilter? = null,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
