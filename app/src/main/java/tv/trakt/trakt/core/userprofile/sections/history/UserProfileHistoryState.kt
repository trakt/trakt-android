package tv.trakt.trakt.core.userprofile.sections.history

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.activity.HomeActivityItem

@Immutable
internal data class UserProfileHistoryState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
