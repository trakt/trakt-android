package tv.trakt.trakt.tv.core.profile.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.HistoryApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import java.time.LocalDate

internal class ProfileApiClient(
    private val api: UsersApi,
    private val calendarsApi: CalendarsApi,
    private val historyApi: HistoryApi,
) : ProfileRemoteDataSource {
    override suspend fun getUserProfile(): User {
        val response = api.getUsersSettings(
            extended = "browsing",
        ).body()

        return User.fromDto(response)
    }

    override suspend fun getUserShowsCalendar(
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

    override suspend fun getUserEpisodesHistory(
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

    override suspend fun getUserMoviesHistory(
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

    override suspend fun getUserFavoriteShows(
        page: Int,
        limit: Int,
    ): List<SyncFavoriteShowDto> {
        val response = api.getUsersFavoritesShows(
            id = "me",
            sort = "added",
            extended = "full,cloud9,colors",
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getUserFavoriteMovies(
        page: Int,
        limit: Int,
    ): List<SyncFavoriteMovieDto> {
        val response = api.getUsersFavoritesMovies(
            id = "me",
            sort = "added",
            extended = "full,cloud9,colors",
            page = page,
            limit = limit,
        )
        return response.body()
    }
}
