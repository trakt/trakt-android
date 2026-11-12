package tv.trakt.trakt.common.data.remote

import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3RealityCheckResponse
import java.util.Locale

class RealityCheckRemoteSource(
    private val v3Api: V3Api,
) {
    suspend fun getRealityCheck(
        type: MediaType,
        mediaId: TraktId,
        locale: Locale,
    ): V3RealityCheckResponse? {
        return try {
            v3Api.getRealityCheck(
                type = type,
                mediaId = mediaId,
                locale = locale,
            )
        } catch (error: Exception) {
            // Titles without a reality check 404 upstream, which means no data rather than a failure.
            if (error.getHttpCode() == HTTP_ERROR_NOT_FOUND) {
                return null
            }
            throw error
        }
    }
}
