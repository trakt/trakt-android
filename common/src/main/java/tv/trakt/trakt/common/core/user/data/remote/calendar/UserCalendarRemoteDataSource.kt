package tv.trakt.trakt.common.core.user.data.remote.calendar

import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import java.time.LocalDate

interface UserCalendarRemoteDataSource {
    suspend fun getShowsCalendar(
        startDate: LocalDate,
        days: Int,
        filters: GlobalFilter? = null,
    ): List<CalendarShowDto>

    suspend fun getMoviesCalendar(
        startDate: LocalDate,
        days: Int,
        filters: GlobalFilter? = null,
    ): List<CalendarMovieDto>
}
