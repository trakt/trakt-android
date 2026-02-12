package tv.trakt.trakt.app.core.plex.data

import tv.trakt.trakt.app.core.plex.PlexStreamApi
import tv.trakt.trakt.app.core.plex.PlexStreamDto
import tv.trakt.trakt.common.model.MediaType

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
