package tv.trakt.trakt.core.calendar.model

import androidx.annotation.DrawableRes
import tv.trakt.trakt.resources.R

internal enum class CalendarView(
    @DrawableRes val iconRes: Int,
) {
    Weekly(
        iconRes = R.drawable.ic_calendar_view_month,
    ),
    Monthly(
        iconRes = R.drawable.ic_calendar_view_week,
    ),
    ;

    val toggle: CalendarView
        get() = when (this) {
            Weekly -> Monthly
            Monthly -> Weekly
        }
}
