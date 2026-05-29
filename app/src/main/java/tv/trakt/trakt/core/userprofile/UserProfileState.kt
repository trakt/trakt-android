package tv.trakt.trakt.core.userprofile

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.thismonth.model.ThisMonthStats

@Immutable
internal data class UserProfileState(
    val user: User,
    val userBlocked: BlockedState = BlockedState(),
    val userFollowing: FollowingState = FollowingState(),
    val monthStats: MonthlyStats? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val info: StringResource? = null,
) {
    data class MonthlyStats(
        val stats: ThisMonthStats?,
        val backgroundUrl: String?,
        val loading: Boolean,
    )

    data class BlockedState(
        val blocked: Boolean = false,
        val loading: Boolean = true,
    )

    data class FollowingState(
        val following: Boolean = false,
        val loading: Boolean = true,
    )
}
