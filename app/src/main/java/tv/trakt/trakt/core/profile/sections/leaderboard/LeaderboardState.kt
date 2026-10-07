package tv.trakt.trakt.core.profile.sections.leaderboard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry

@Immutable
internal data class LeaderboardState(
    val items: ImmutableList<LeaderboardEntry>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
