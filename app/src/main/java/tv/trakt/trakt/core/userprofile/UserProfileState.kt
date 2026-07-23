package tv.trakt.trakt.core.userprofile

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import tv.trakt.trakt.core.user.model.UserFollowRequest

@Immutable
internal data class UserProfileState(
    val user: User,
    val isCurrentUser: Boolean = false,
    val accessChecking: Boolean = user.isPrivate,
    val userBlocked: BlockedState = BlockedState(),
    val userFollowing: FollowingState = FollowingState(),
    val userRequest: UserFollowRequestState = UserFollowRequestState(),
    val monthStats: MonthlyStats? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val info: StringResource? = null,
) {
    val access: Access
        get() = when {
            !user.isPrivate || isCurrentUser -> Access.Granted
            accessChecking -> Access.Checking
            userFollowing.following -> Access.Granted
            else -> Access.Denied
        }

    enum class Access {
        Checking,
        Granted,
        Denied,
    }

    data class MonthlyStats(
        val stats: ProfileStats?,
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

    data class UserFollowRequestState(
        val request: UserFollowRequest? = null,
        val loading: Boolean = false,
    )
}
