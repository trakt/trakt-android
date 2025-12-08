package tv.trakt.trakt.core.user.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.HistoryApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PutUsersSaveSettingsRequest
import org.openapitools.client.models.PutUsersSaveSettingsRequestUser
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import tv.trakt.trakt.common.networking.UserCommentsDto
import tv.trakt.trakt.common.networking.UserRatingDto
import tv.trakt.trakt.common.networking.WatchedShowDto
import tv.trakt.trakt.common.networking.WatchlistItemDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.time.LocalDate
import java.time.ZonedDateTime

internal class UserApiClient(
    private val usersApi: UsersApi,
    private val historyApi: HistoryApi,
    private val calendarsApi: CalendarsApi,
    private val cacheMarkerProvider: CacheMarkerProvider,
) : UserRemoteDataSource {
    override suspend fun getProfile(): User {
        val response = usersApi.getUsersSettings(
            extended = "browsing",
        ).body()

        return User.fromDto(response)
    }

    override suspend fun updateProfileLocation(location: String?) {
        usersApi.putUsersSaveSettings(
            putUsersSaveSettingsRequest = PutUsersSaveSettingsRequest(
                user = PutUsersSaveSettingsRequestUser(
                    location = location,
                ),
            ),
        )

        cacheMarkerProvider.invalidate()
    }

    override suspend fun updateProfileDisplayName(displayName: String?) {
        usersApi.putUsersSaveSettings(
            putUsersSaveSettingsRequest = PutUsersSaveSettingsRequest(
                user = PutUsersSaveSettingsRequestUser(
                    name = displayName,
                ),
            ),
        )

        cacheMarkerProvider.invalidate()
    }

    override suspend fun updateProfileAbout(about: String?) {
        usersApi.putUsersSaveSettings(
            putUsersSaveSettingsRequest = PutUsersSaveSettingsRequest(
                user = PutUsersSaveSettingsRequestUser(
                    about = about,
                ),
            ),
        )

        cacheMarkerProvider.invalidate()
    }

    override suspend fun getWatchedMovies(): Map<String, List<String>> {
        val response = usersApi.getUsersWatchedMinimalMovies(
            id = "me",
            extended = "min",
        )

        return response.body()
    }

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
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            hide = null,
        )

        return response.body()
    }

    override suspend fun getWatchlistMovies(
        page: Int?,
        limit: Int?,
        extended: String?,
        sort: String?,
        hide: String?,
    ): List<WatchlistMovieDto> {
        val response = usersApi.getUsersWatchlistMovies(
            id = "me",
            sort = sort ?: "rank",
            extended = extended,
            page = page,
            limit = limit,
            hide = hide,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getRatingsShows(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsShows(
            id = "me",
            extended = null,
        )

        return response.body()
    }

    override suspend fun getRatingsMovies(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsMovies(
            id = "me",
            extended = null,
        )

        return response.body()
    }

    override suspend fun getRatingsEpisodes(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsEpisodes(
            id = "me",
            extended = null,
        )

        return response.body()
    }

    override suspend fun getFavoriteShows(
        page: Int?,
        limit: Int?,
        extended: String?,
        sort: String?,
    ): List<SyncFavoriteShowDto> {
        val response = usersApi.getUsersFavoritesShows(
            id = "me",
            extended = extended,
            page = page,
            limit = 99999,
            sort = sort ?: "rank",
        )

        return response.body()
    }

    override suspend fun getFavoriteMovies(
        page: Int?,
        limit: Int?,
        extended: String?,
        sort: String?,
    ): List<SyncFavoriteMovieDto> {
        val response = usersApi.getUsersFavoritesMovies(
            id = "me",
            extended = extended,
            page = page,
            limit = 99999,
            sort = sort ?: "rank",
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

    override suspend fun getEpisodeHistory(
        episodeId: TraktId,
        page: Int,
        limit: Int?,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisode(
            id = "me",
            itemId = episodeId.value.toString(),
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
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate2 = null,
            endDate = null,
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
        )
        return response.body()
    }

    override suspend fun getPersonalLists(): List<ListDto> {
        val response = usersApi.getUsersListsPersonal(
            id = "me",
            extended = "cloud9",
            page = null,
            limit = null,
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
            sortBy = "rank",
            sortHow = "asc",
            watchnow = null,
            genres = null,
            years = null,
            subgenres = null,
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

    override suspend fun getFollowers(): Map<UserCommentsDto, ZonedDateTime> {
        val response = usersApi.getUsersFollowers(
            id = "me",
            extended = "full,vip",
        ).body()

        return response.associate {
            val followedAt = it.followedAt.toZonedDateTime()
            it.user to followedAt
        }
    }

    override suspend fun getFollowing(): Map<UserCommentsDto, ZonedDateTime> {
        val response = usersApi.getUsersFollowing(
            id = "me",
            extended = "full,vip",
        ).body()

        return response.associate {
            val followedAt = it.followedAt.toZonedDateTime()
            it.user to followedAt
        }
    }
}
