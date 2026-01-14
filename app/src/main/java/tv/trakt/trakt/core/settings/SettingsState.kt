package tv.trakt.trakt.core.settings

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment

@Immutable
internal data class SettingsState(
    val user: User? = null,
    val notifications: Boolean = false,
    val notificationsDelivery: DeliveryAdjustment? = null,
    val accountLoading: LoadingState = LoadingState.IDLE,
    val logoutLoading: LoadingState = LoadingState.IDLE,
)
