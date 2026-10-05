package tv.trakt.trakt.core.parentalguide.data.remote

import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.parentalguide.ParentalGuide
import tv.trakt.trakt.common.networking.api.v3.V3Api

internal class ParentalGuideApiClient(
    private val v3Api: V3Api,
) : ParentalGuideRemoteDataSource {
    override suspend fun getParentalGuide(
        type: MediaType,
        mediaId: TraktId,
    ): ParentalGuide {
        val response = try {
            v3Api.getParentalGuide(type, mediaId)
        } catch (error: Exception) {
            // A missing guide 404s upstream, which means no guide rather than a failure.
            if (error.getHttpCode() == HTTP_ERROR_NOT_FOUND) {
                return ParentalGuide()
            }
            throw error
        }

        return response
            ?.let(ParentalGuide::fromDto)
            ?: ParentalGuide()
    }
}
