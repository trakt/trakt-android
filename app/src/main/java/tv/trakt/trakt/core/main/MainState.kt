package tv.trakt.trakt.core.main

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class MainState(
    val user: User? = null,
    val userVipStatus: Pair<Boolean?, Boolean?>? = null,
    val loadingUser: LoadingState = LoadingState.IDLE,
    val welcome: Boolean = false,
)
