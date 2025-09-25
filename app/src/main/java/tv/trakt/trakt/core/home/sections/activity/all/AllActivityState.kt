package tv.trakt.trakt.core.home.sections.activity.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

@Immutable
internal data class AllActivityState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingMore: LoadingState = LoadingState.IDLE,
    val backgroundUrl: String? = null,
    val user: User? = null,
    val error: Exception? = null,
)
