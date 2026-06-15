package tv.trakt.trakt.core.profile

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats

@Immutable
internal data class ProfileState(
    val user: User? = null,
    val monthBackgroundUrl: String? = null,
    val monthStats: ProfileStats? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMonthStats: LoadingState = LoadingState.Idle,
    val logoutLoading: LoadingState = LoadingState.Idle,
    val checkIn: Boolean = false,
)
