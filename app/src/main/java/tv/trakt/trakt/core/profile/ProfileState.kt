package tv.trakt.trakt.core.profile

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats

@Immutable
internal data class ProfileState(
    val mode: MediaMode = MediaMode.Media,
    val user: User? = null,
    val monthBackgroundUrl: String? = null,
    val monthStats: ProfileStats? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMonthStats: LoadingState = LoadingState.Idle,
    val checkIn: Boolean = false,
    val hasLeaderboard: Boolean = false,
    val loadingLeaderboard: LoadingState = LoadingState.Idle,
    val leaderboardUsers: ImmutableList<User> = persistentListOf(),
    val followingCount: Int? = null,
)
