package tv.trakt.trakt.tv.core.sync.data.remote.shows

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.apis.WatchedApi
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInnerOneOfIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.common.networking.WatchedShowDto
import tv.trakt.trakt.common.networking.WatchlistShowDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_INSTANT

internal class ShowsSyncApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
    private val watchedApi: WatchedApi,
) : ShowsSyncRemoteDataSource {
    override suspend fun addToWatchlist(showId: TraktId) {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = null,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistAdd(request)
    }

    override suspend fun removeFromWatchlist(showId: TraktId) {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = null,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistRemove(request)
    }

    override suspend fun getWatchlist(
        sort: String,
        page: Int?,
        limit: Int?,
        extended: String?,
    ): List<WatchlistShowDto> {
        val response = usersApi.getUsersWatchlistShows(
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

    override suspend fun getUpNextProgress(limit: Int): List<ProgressShowDto> {
        val response = syncApi.getSyncProgressUpNextNitro(
            page = null,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun addToHistory(
        showId: TraktId,
        watchedAt: ZonedDateTime,
    ): SyncAddHistoryResponseDto {
        val request = PostUsersListsListAddRequest(
            shows = listOf(
                PostUsersListsListAddRequestShowsInner(
                    ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = null,
                        tvdb = 0,
                    ),
                    title = "",
                    year = 0,
                    watchedAt = watchedAt.format(ISO_INSTANT),
                ),
            ),
        )

        val result = syncApi.postSyncHistoryAdd(request)
        return result.body()
    }

    override suspend fun getWatched(extended: String?): List<WatchedShowDto> {
        val response = watchedApi.getUsersWatchedShows(
            id = "me",
            extended = extended,
            hidden = null,
            specials = true,
            countSpecials = null,
        )
        return response.body()
    }
}
