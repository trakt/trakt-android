package tv.trakt.trakt.core.notifications.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class DeliveryAdjustment(
    val duration: Duration,
    @param:StringRes val displayString: Int,
) {
    OFF(Duration.ZERO, R.string.text_settings_notification_time_now),
    MINUTES_30(30.minutes, R.string.text_settings_notification_time_30_min),
    MINUTES_60(1.hours, R.string.text_settings_notification_time_60_min),
    MINUTES_180(3.hours, R.string.text_settings_notification_time_180_min),
}
