package tv.trakt.trakt.core.profile

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.thismonth.model.ThisMonthStats

@Immutable
internal data class ProfileState(
    val user: User? = null,
    val backgroundUrl: String? = null,
    val monthBackgroundUrl: String? = null,
    val monthStats: ThisMonthStats? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingMonthStats: LoadingState = LoadingState.IDLE,
)
