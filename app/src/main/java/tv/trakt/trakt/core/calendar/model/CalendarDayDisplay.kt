package tv.trakt.trakt.core.calendar.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class CalendarDayDisplay(
    @StringRes val textRes: Int,
) {
    Posters(
        textRes = R.string.button_text_calendar_items_as_icons,
    ),
    Labels(
        textRes = R.string.button_text_calendar_items_as_text,
    ),
    ;

    val toggle: CalendarDayDisplay
        get() = when (this) {
            Posters -> Labels
            Labels -> Posters
        }
}
