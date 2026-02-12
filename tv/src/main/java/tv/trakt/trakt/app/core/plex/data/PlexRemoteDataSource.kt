package tv.trakt.trakt.app.core.plex.data

import tv.trakt.trakt.app.core.plex.PlexStreamDto
import tv.trakt.trakt.common.model.MediaType

internal interface PlexRemoteDataSource {
    suspend fun getPlexStream(
        id: String,
        type: MediaType,
    ): PlexStreamDto
}
