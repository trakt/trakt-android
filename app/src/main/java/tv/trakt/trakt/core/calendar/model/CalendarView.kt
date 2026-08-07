package tv.trakt.trakt.core.calendar.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class CalendarView(
    @StringRes val textRes: Int,
    @DrawableRes val iconRes: Int,
) {
    Weekly(
        textRes = R.string.button_text_calendar_view_weekly,
        iconRes = R.drawable.ic_calendar_view_week,
    ),
    Monthly(
        textRes = R.string.button_text_calendar_view_monthly,
        iconRes = R.drawable.ic_calendar_view_month,
    ),
    ;

    val toggle: CalendarView
        get() = when (this) {
            Weekly -> Monthly
            Monthly -> Weekly
        }
}
