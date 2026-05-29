package tv.trakt.trakt.core.userprofile.sections.social

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter

@Immutable
internal data class UserProfileSocialState(
    val items: ImmutableList<User>? = null,
    val filter: SocialFilter = SocialFilter.FOLLOWING,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
