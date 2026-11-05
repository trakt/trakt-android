package tv.trakt.trakt.common.data.remote

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.common.networking.api.v3.V3Api
import java.util.Locale

class SoundtrackRemoteSource(
    private val v3Api: V3Api,
) {
    suspend fun getSoundtrack(
        type: MediaType,
        mediaId: TraktId,
        locale: Locale,
    ): List<SoundtrackTrack> {
        return v3Api
            .getSoundtrack(
                type = type,
                mediaId = mediaId,
                locale = locale,
            )
            .map(SoundtrackTrack::fromDto)
            .sortedBy { it.position }
    }
}
