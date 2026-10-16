package tv.trakt.trakt.common.core.plex.data.remote

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.networking.api.plex.PlexStreamDto

interface PlexRemoteDataSource {
    suspend fun getPlexStream(
        id: String,
        type: MediaType,
    ): PlexStreamDto
}
