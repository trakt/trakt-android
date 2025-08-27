package tv.trakt.trakt.core.home.sections.activity

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.home.sections.activity.model.SocialActivityItem

@Immutable
internal data class HomeSocialState(
    val items: ImmutableList<SocialActivityItem>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
