package tv.trakt.trakt.core.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.rememberAppLocale
import tv.trakt.trakt.common.helpers.extensions.yearMonthFormat
import java.time.YearMonth
import java.time.format.TextStyle.FULL_STANDALONE

/**
 * Month name for the calendar header, with the year added only when it differs
 * from the current one.
 */
@Composable
internal fun calendarMonthTitle(month: YearMonth): String {
    val locale = rememberAppLocale()
    val monthFormat = yearMonthFormat()
    val currentYear = remember { nowLocalDay().year }

    return remember(month, locale, monthFormat) {
        when (month.year) {
            currentYear -> month.month.getDisplayName(FULL_STANDALONE, locale)
            else -> month.format(monthFormat)
        }.capitalize()
    }
}
