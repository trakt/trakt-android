package tv.trakt.trakt.core.settings

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment
import tv.trakt.trakt.ui.theme.model.ThemeMode

@Immutable
internal data class SettingsState(
    val user: User? = null,
    val notifications: Boolean = false,
    val notificationsDelivery: DeliveryAdjustment? = null,
    val themeMode: ThemeMode = ThemeMode.Default,
    val accountLoading: LoadingState = LoadingState.Idle,
    val logoutLoading: LoadingState = LoadingState.Idle,
    val info: StringResource? = null,
)
