package tv.trakt.trakt.core.settings.categories.notifications

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment

@Immutable
internal data class NotificationSettingsState(
    val enabled: Boolean = false,
    val delivery: DeliveryAdjustment? = null,
)
