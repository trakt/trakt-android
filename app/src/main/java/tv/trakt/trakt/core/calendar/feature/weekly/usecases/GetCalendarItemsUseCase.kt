package tv.trakt.trakt.core.calendar.feature.weekly.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.usecases.CalendarItemsLoader
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

internal class GetCalendarItemsUseCase(
    private val itemsLoader: CalendarItemsLoader,
    private val sessionManager: SessionManager,
) {
    suspend fun getCalendarItems(
        day: LocalDate,
        filters: GlobalFilter,
        type: ReleaseType,
    ): ImmutableMap<LocalDate, ImmutableList<CalendarItem>> {
        if (!sessionManager.isAuthenticated()) {
            return persistentMapOf()
        }

        val (weekStart, weekEnd) = with(day) {
            with(MONDAY) to with(SUNDAY)
        }

        return itemsLoader.load(
            range = weekStart..weekEnd,
            filters = filters,
            type = type,
        )
    }
}
