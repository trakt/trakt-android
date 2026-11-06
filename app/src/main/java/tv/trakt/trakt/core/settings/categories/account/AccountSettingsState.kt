package tv.trakt.trakt.core.settings.categories.account

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class AccountSettingsState(
    val user: User? = null,
    val loading: LoadingState = LoadingState.Idle,
)
