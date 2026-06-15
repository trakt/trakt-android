package tv.trakt.trakt.core.profile

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.thismonth.model.ThisMonthStats
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey

@Immutable
internal data class ProfileState(
    val user: User? = null,
    val monthBackgroundUrl: String? = null,
    val monthStats: ThisMonthStats? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMonthStats: LoadingState = LoadingState.Idle,
    val logoutLoading: LoadingState = LoadingState.Idle,
    val checkIn: Boolean = false,
    val visibility: ImmutableMap<EditScreenKey, Boolean>? = null,
)
