package tv.trakt.trakt.core.calendar.feature.weekly.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.calendar.usecases.CalendarItemsLoader
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

internal class GetCalendarItemsUseCase(
    private val itemsLoader: CalendarItemsLoader,
) {
    suspend fun getCalendarItems(
        source: CalendarSource,
        day: LocalDate,
        filters: GlobalFilter,
        type: ReleaseType,
        skipProgress: Boolean = false,
    ): ImmutableMap<LocalDate, ImmutableList<CalendarItem>> {
        val (weekStart, weekEnd) = with(day) {
            with(MONDAY) to with(SUNDAY)
        }

        return itemsLoader.load(
            source = source,
            range = weekStart..weekEnd,
            filters = filters,
            type = type,
            skipProgress = skipProgress,
        )
    }
}
