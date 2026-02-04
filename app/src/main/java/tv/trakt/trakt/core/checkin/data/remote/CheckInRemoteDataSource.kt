package tv.trakt.trakt.core.checkin.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CheckInMovieResponseDto

interface CheckInRemoteDataSource {
    suspend fun postMovieCheckIn(movieId: TraktId): CheckInMovieResponseDto

    suspend fun deleteAll()
}
