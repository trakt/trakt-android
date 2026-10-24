package tv.trakt.trakt.core.calendar.feature.monthly.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.calendar.usecases.CalendarItemsLoader
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters.nextOrSame
import java.time.temporal.TemporalAdjusters.previousOrSame

/**
 * Loads calendar items for every day the monthly grid renders - the month itself
 * plus the leading and trailing days it borrows from the neighbouring months to
 * fill the first and last week rows (35 or 42 days).
 */
internal class GetMonthlyCalendarItemsUseCase(
    private val itemsLoader: CalendarItemsLoader,
) {
    suspend fun getCalendarItems(
        source: CalendarSource,
        month: YearMonth,
        filters: GlobalFilter,
        type: ReleaseType,
        firstDayOfWeek: DayOfWeek = MONDAY,
    ): ImmutableMap<LocalDate, ImmutableList<CalendarItem>> {
        return itemsLoader.load(
            source = source,
            range = month.visibleDays(firstDayOfWeek),
            filters = filters,
            type = type,
        )
    }
}

private fun YearMonth.visibleDays(firstDayOfWeek: DayOfWeek): ClosedRange<LocalDate> {
    val first = atDay(1).with(previousOrSame(firstDayOfWeek))
    val last = atEndOfMonth().with(nextOrSame(firstDayOfWeek.plus(6)))

    return first..last
}
