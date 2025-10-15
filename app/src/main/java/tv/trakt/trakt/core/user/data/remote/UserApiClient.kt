package tv.trakt.trakt.core.user.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.HistoryApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import tv.trakt.trakt.common.networking.WatchedMovieDto
import tv.trakt.trakt.common.networking.WatchedShowDto
import tv.trakt.trakt.common.networking.WatchlistItemDto
import tv.trakt.trakt.common.networking.api.SyncExtrasApi
import java.time.LocalDate

internal class UserApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncExtrasApi,
    private val historyApi: HistoryApi,
    private val calendarsApi: CalendarsApi,
) : UserRemoteDataSource {
    override suspend fun getProfile(): User {
        val response = usersApi.getUsersSettings(
            extended = "browsing",
        ).body()

        return User.fromDto(response)
    }

    override suspend fun getWatchedMovies(): List<WatchedMovieDto> {
        val response = usersApi.getUsersWatchedMovies(
            "me",
        )

        return response.body()
    }

//    override suspend fun getProgressShows(limit: Int?): List<SyncProgressItemDto> {
//        val allResults = mutableListOf<SyncProgressItemDto>()
//        val defaultLimit = 200
//
//        var page = 1
//        var hasMorePages = true
//
//        while (hasMorePages) {
//            val response = syncApi.getProgressWatched(
//                limit = when {
//                    limit == null -> "all"
//                    else -> limit.toString()
//                },
//                page = page,
//            )
//
//            val body = response.body()
//            if (body.isNotEmpty()) {
//                allResults.addAll(body)
//                page++
//            }
//
//            if (limit == null || body.size < defaultLimit) {
//                hasMorePages = false
//            }
//        }
//
//        return allResults
//    }

    override suspend fun getWatchedShows(): List<WatchedShowDto> {
        val response = usersApi.getUsersWatchedShows(
            id = "me",
            extended = null,
            hidden = null,
            specials = true,
            countSpecials = null,
        )

        return response.body()
    }

    override suspend fun getWatchlist(
        page: Int?,
        limit: Int?,
        extended: String?,
        sort: String?,
    ): List<WatchlistItemDto> {
        val response = usersApi.getUsersWatchlistAll(
            id = "me",
            sort = sort ?: "rank",
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
        page: Int?,
        limit: Int,
        type: String,
    ): List<SocialActivityItemDto> {
        val response = usersApi.getUsersActivities(
            id = "me", // or use the current user ID
            type = type,
            page = page,
            limit = limit,
            extended = "full,cloud9,colors",
        ).body()

        return response
    }

    override suspend fun getEpisodesHistory(
        page: Int,
        limit: Int,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisodes(
            id = "me",
            extended = "full,cloud9,colors",
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
            extended = "full,cloud9,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getMovieHistory(
        movieId: TraktId,
        page: Int,
        limit: Int,
    ): List<SyncHistoryMovieItemDto> {
        val response = historyApi.getUsersHistoryMovie(
            id = "me",
            itemId = movieId.value.toString(),
            extended = "full,cloud9,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getShowHistory(
        showId: TraktId,
        page: Int,
        limit: Int?,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryShow(
            id = "me",
            itemId = showId.value.toString(),
            extended = "full,cloud9,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = when {
                limit == null -> 99_999
                else -> limit
            },
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

    override suspend fun getPersonalLists(): List<ListDto> {
        val response = usersApi.getUsersListsPersonal(
            id = "me",
            extended = "cloud9",
        )
        return response.body()
    }

    override suspend fun getPersonalListItems(
        listId: TraktId,
        limit: Int?,
        page: Int,
        extended: String,
    ): List<ListItemDto> {
        val response = usersApi.getUsersListsListItemsAll(
            id = "me",
            listId = listId.value.toString(),
            extended = extended,
            sortBy = "added",
            sortHow = "asc",
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            page = page,
            limit = when {
                limit == null -> "all"
                else -> limit.toString()
            },
        )

        return response.body()
    }
}
