package tv.trakt.trakt.core.main

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class MainState(
    val user: User? = null,
    val userVipStatus: Pair<Boolean?, Boolean?>? = null,
    val loadingUser: LoadingState = LoadingState.IDLE,
    val welcome: WelcomeState = WelcomeState(),
) {
    @Immutable
    data class WelcomeState(
        val welcome: Boolean = false,
        val onboarding: Boolean = false,
    ) {
        val isActive: Boolean
            get() = welcome || onboarding
    }
}
