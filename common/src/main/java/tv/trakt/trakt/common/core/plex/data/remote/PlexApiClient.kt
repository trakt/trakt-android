package tv.trakt.trakt.common.core.plex.data.remote

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.networking.api.plex.PlexStreamApi
import tv.trakt.trakt.common.networking.api.plex.PlexStreamDto

internal class PlexApiClient(
    private val api: PlexStreamApi,
) : PlexRemoteDataSource {
    override suspend fun getPlexStream(
        id: String,
        type: MediaType,
    ): PlexStreamDto {
        val response = api.getPlexStream(
            id = id,
            type = "${type.value}s",
        )
        return response.body()
    }
}
