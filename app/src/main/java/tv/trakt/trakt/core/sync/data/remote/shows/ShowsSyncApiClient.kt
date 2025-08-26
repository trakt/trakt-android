package tv.trakt.trakt.core.sync.data.remote.shows

import org.openapitools.client.apis.SyncApi
import tv.trakt.trakt.common.networking.ProgressShowDto

internal class ShowsSyncApiClient(
    private val syncApi: SyncApi,
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
}
