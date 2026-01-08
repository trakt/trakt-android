package tv.trakt.trakt.core.profile.sections.social

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter

@Immutable
internal data class ProfileSocialState(
    val user: User? = null,
    val items: ImmutableList<User>? = null,
    val filter: SocialFilter = SocialFilter.FOLLOWING,
    val loading: LoadingState = LoadingState.IDLE,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
