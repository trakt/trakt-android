package tv.trakt.trakt.core.settings.categories.general

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class GeneralSettingsState(
    val user: User? = null,
    val loading: LoadingState = LoadingState.Idle,
    val logoutLoading: LoadingState = LoadingState.Idle,
)
