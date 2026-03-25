package tv.trakt.trakt.common.core.user.data.remote.calendar

import org.openapitools.client.apis.CalendarsApi
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import java.time.LocalDate

class UserCalendarApiClient(
    private val calendarsApi: CalendarsApi,
) : UserCalendarRemoteDataSource {
    override suspend fun getShowsCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarShowDto> {
        val response = calendarsApi.getCalendarsShows(
            target = "my",
            startDate = startDate.toString(),
            days = days,
            extended = "full,cloud9,colors",
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate2 = null,
            endDate = null,
            runtimes = null,
        )
        return response.body()
    }

    override suspend fun getMoviesCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarMovieDto> {
        val response = calendarsApi.getCalendarsMovies(
            target = "my",
            startDate = startDate.toString(),
            days = days,
            extended = "full,cloud9,colors",
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate2 = null,
            endDate = null,
            runtimes = null,
        )
        return response.body()
    }
}
