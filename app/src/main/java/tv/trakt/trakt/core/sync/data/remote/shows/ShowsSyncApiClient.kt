package tv.trakt.trakt.core.sync.data.remote.shows

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInnerOneOfIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.WatchlistShowDto

internal class ShowsSyncApiClient(
    private val syncApi: SyncApi,
    private val usersApi: UsersApi,
) : ShowsSyncRemoteDataSource {
    override suspend fun getUpNext(
        limit: Int,
        page: Int,
    ): List<ProgressShowDto> {
        val response = syncApi.getSyncProgressUpNextNitro(
            page = page,
            limit = limit,
        )
        return response.body()
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

    override suspend fun dropShow(showId: TraktId) {
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

        coroutineScope {
            val droppedAsync = async {
                usersApi.postUsersHiddenAdd(
                    section = "dropped",
                    postUsersListsListAddRequest = request,
                )
            }
            val calendarAsync = async {
                usersApi.postUsersHiddenAdd(
                    section = "calendar",
                    postUsersListsListAddRequest = request,
                )
            }
            droppedAsync.await()
            calendarAsync.await()
        }
    }
}
