package tv.trakt.trakt.common.core.parentalguide.data.remote

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.parentalguide.ParentalGuide

interface ParentalGuideRemoteDataSource {
    suspend fun getParentalGuide(
        type: MediaType,
        mediaId: TraktId,
    ): ParentalGuide
}
