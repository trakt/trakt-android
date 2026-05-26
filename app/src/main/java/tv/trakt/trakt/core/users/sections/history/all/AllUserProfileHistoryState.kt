package tv.trakt.trakt.core.users.sections.history.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import java.time.LocalDate

@Immutable
internal data class AllUserProfileHistoryState(
    val items: ImmutableMap<LocalDate, ImmutableList<HomeActivityItem>>? = null,
    val filters: GlobalFilter = GlobalFilter.Default,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
