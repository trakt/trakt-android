package tv.trakt.trakt.core.checkin.data.remote

import tv.trakt.trakt.common.model.TraktId

interface CheckInRemoteDataSource {
    suspend fun postMovieCheckIn(movieId: TraktId)

    suspend fun postEpisodeCheckIn(
        showId: TraktId,
        season: Int,
        episode: Int,
    )

    suspend fun deleteAll()
}
