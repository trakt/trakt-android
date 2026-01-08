package tv.trakt.trakt.core.profile.sections.history

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

@Immutable
internal data class ProfileHistoryState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
