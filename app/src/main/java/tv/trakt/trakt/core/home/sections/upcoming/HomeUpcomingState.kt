package tv.trakt.trakt.core.home.sections.upcoming

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class HomeUpcomingState(
    val items: ImmutableList<HomeUpcomingItem>? = null,
    val filter: MediaMode? = null,
    val collapsed: Boolean? = null,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
