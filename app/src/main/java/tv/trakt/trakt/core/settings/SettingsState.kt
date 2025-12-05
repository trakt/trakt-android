package tv.trakt.trakt.core.settings

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class SettingsState(
    val user: User? = null,
    val accountLoading: LoadingState = LoadingState.IDLE,
    val logoutLoading: LoadingState = LoadingState.IDLE,
)
