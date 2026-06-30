package tv.trakt.trakt.core.profile.sections.social

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter
import tv.trakt.trakt.core.user.model.UserFollowRequest

@Immutable
internal data class ProfileSocialState(
    val user: User? = null,
    val items: ImmutableList<User>? = null,
    val requests: ImmutableList<UserFollowRequest>? = null,
    val filter: SocialFilter = SocialFilter.Following,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
