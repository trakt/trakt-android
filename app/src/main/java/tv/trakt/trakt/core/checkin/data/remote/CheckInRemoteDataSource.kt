package tv.trakt.trakt.core.checkin.data.remote

import org.openapitools.client.models.PostCheckinStart200Response
import tv.trakt.trakt.common.model.TraktId

interface CheckInRemoteDataSource {
    suspend fun postMovieCheckIn(movieId: TraktId): PostCheckinStart200Response?

    suspend fun postEpisodeCheckIn(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): PostCheckinStart200Response?

    suspend fun deleteAll()
}
