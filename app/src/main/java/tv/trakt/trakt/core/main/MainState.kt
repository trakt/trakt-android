package tv.trakt.trakt.core.main

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.WhatsNew
import tv.trakt.trakt.core.checkin.model.CheckInState

@Immutable
internal data class MainState(
    val user: User? = null,
    val userVipStatus: Pair<Boolean?, Boolean?>? = null,
    val checkIn: CheckInState? = null,
    val loadingUser: LoadingState = LoadingState.Idle,
    val welcome: WelcomeState = WelcomeState(),
    val whatsNew: WhatsNew? = null,
    val review: Boolean? = null,
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
