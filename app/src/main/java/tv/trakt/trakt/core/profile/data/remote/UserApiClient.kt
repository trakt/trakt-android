package tv.trakt.trakt.core.profile.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.HistoryApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import tv.trakt.trakt.common.networking.WatchlistItemDto
import java.time.LocalDate

internal class UserApiClient(
    private val usersApi: UsersApi,
    private val historyApi: HistoryApi,
    private val calendarsApi: CalendarsApi,
) : UserRemoteDataSource {
    override suspend fun getProfile(): User {
        val response = usersApi.getUsersSettings(
            extended = "browsing",
        ).body()

        return User.fromDto(response)
    }

    override suspend fun getWatchlist(
        page: Int,
        limit: Int,
        sort: String,
        extended: String,
    ): List<WatchlistItemDto> {
        val response = usersApi.getUsersWatchlistAll(
            id = "me",
            sort = sort,
            extended = extended,
            page = page,
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getSocialActivity(
        limit: Int,
        type: String,
    ): List<SocialActivityItemDto> {
        val response = usersApi.getUsersActivities(
            id = "me", // or use the current user ID
            type = type,
            limit = limit,
            extended = "full,cloud9",
            page = 1,
        ).body()

        return response
    }

    override suspend fun getEpisodesHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisodes(
            id = "me",
            extended = "full,cloud9",
            startAt = null,
            endAt = null,
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getMoviesHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryMovieItemDto> {
        val response = historyApi.getUsersHistoryMovies(
            id = "me",
            extended = "full,cloud9",
            startAt = null,
            endAt = null,
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getShowsCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarShowDto> {
        val response = calendarsApi.getCalendarsShows(
            target = "my",
            startDate = startDate.toString(),
            days = days,
            extended = "full,cloud9,colors",
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
        )
        return response.body()
    }
}
