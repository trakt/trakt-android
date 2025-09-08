package tv.trakt.trakt.core.sync.data.remote.shows

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
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
}
