package tv.trakt.trakt.common.data.remote

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3SoundtrackTrackResponse
import java.util.Locale

class SoundtrackRemoteSource(
    private val v3Api: V3Api,
) {
    suspend fun getSoundtrack(
        type: MediaType,
        mediaId: TraktId,
        locale: Locale,
    ): List<V3SoundtrackTrackResponse> {
        return v3Api.getSoundtrack(
            type = type,
            mediaId = mediaId,
            locale = locale,
        )
    }
}
