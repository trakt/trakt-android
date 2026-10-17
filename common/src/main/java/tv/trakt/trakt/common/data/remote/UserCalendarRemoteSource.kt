package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.CalendarsApi
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import java.time.LocalDate

class UserCalendarRemoteSource(
    private val calendarsApi: CalendarsApi,
) {
    suspend fun getShowsCalendar(
        startDate: LocalDate,
        days: Int,
        filters: GlobalFilter? = null,
        extended: String = "full,images,colors",
    ): List<CalendarShowDto> {
        val response = calendarsApi.getCalendarsShows(
            target = "my",
            startDate = startDate.toString(),
            days = days,
            extended = extended,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate2 = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = null,
            group = null,
        )
        return response.body()
    }

    suspend fun getMoviesCalendar(
        startDate: LocalDate,
        days: Int,
        filters: GlobalFilter? = null,
        extended: String = "full,images,colors",
    ): List<CalendarMovieDto> {
        val response = calendarsApi.getCalendarsMovies(
            target = "my",
            startDate = startDate.toString(),
            days = days,
            extended = extended,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate2 = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = null,
        )
        return response.body()
    }
}
