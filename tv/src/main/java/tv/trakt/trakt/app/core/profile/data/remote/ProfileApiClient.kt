package tv.trakt.trakt.app.core.profile.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.HistoryApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.LikedListDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import java.time.LocalDate

internal class ProfileApiClient(
    private val usersApi: UsersApi,
    private val calendarsApi: CalendarsApi,
    private val historyApi: HistoryApi,
) : ProfileRemoteDataSource {
    override suspend fun getUserProfile(): User {
        val response = usersApi.getUsersSettings(
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
            extended = "full,cloud9,colors,streaming_ids",
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

    override suspend fun getUserMoviesCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarMovieDto> {
        val response = calendarsApi.getCalendarsMovies(
            target = "my",
            startDate = startDate.toString(),
            days = days,
            extended = "full,cloud9,colors,streaming_ids",
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

    override suspend fun getUserEpisodesHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisodes(
            id = "me",
            extended = "full,cloud9,streaming_ids",
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
            extended = "full,cloud9,streaming_ids",
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
        val response = usersApi.getUsersFavoritesShows(
            id = "me",
            sort = "added",
            extended = "full,cloud9,colors,streaming_ids",
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getUserFavoriteMovies(
        page: Int,
        limit: Int,
    ): List<SyncFavoriteMovieDto> {
        val response = usersApi.getUsersFavoritesMovies(
            id = "me",
            sort = "added",
            extended = "full,cloud9,colors,streaming_ids",
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getUserLists(): List<ListDto> {
        val response = usersApi.getUsersListsPersonal(
            id = "me",
            extended = "images",
            page = null,
            limit = 100,
        )
        return response.body()
    }

    override suspend fun getLikedLists(
        minimal: Boolean,
        pagination: Pagination,
    ): List<LikedListDto> {
        val response = usersApi.getUsersLikesLists(
            extended = when {
                minimal -> "min"
                else -> "cloud9,images"
            },
            page = pagination.page,
            limit = pagination.limit.toString(),
        )
        return response.body()
    }

    override suspend fun getUserMovieListItems(
        listId: TraktId,
        limit: Int,
        page: Int,
        extended: String,
    ): List<ListMovieItemDto> {
        val response = usersApi.getUsersListsListItemsMovie(
            id = "me",
            listId = listId.value.toString(),
            limit = limit.toString(),
            page = page,
            extended = extended,
            sortBy = null,
            sortHow = null,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            subgenres = null,
            runtimes = null,
        )
        return response.body()
    }

    override suspend fun getUserShowListItems(
        listId: TraktId,
        limit: Int,
        page: Int,
        extended: String,
    ): List<ListShowItemDto> {
        val response = usersApi.getUsersListsListItemsShow(
            id = "me",
            listId = listId.value.toString(),
            limit = limit.toString(),
            page = page,
            extended = extended,
            sortBy = null,
            sortHow = null,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            subgenres = null,
            runtimes = null,
        )
        return response.body()
    }

    override suspend fun getUserSocialActivity(
        limit: Int,
        type: String,
    ): List<SocialActivityItemDto> {
        val response = usersApi.getUsersActivities(
            id = "me",
            type = type,
            extended = "full,cloud9,streaming_ids",
            page = 1,
            limit = limit,
        )
        return response.body()
    }
}
