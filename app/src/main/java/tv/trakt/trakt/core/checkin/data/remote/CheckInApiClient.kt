package tv.trakt.trakt.core.checkin.data.remote

import org.openapitools.client.apis.CheckinApi
import org.openapitools.client.models.PostCheckinMovieRequest
import org.openapitools.client.models.PostCheckinMovieRequestMovie
import org.openapitools.client.models.PostCheckinMovieRequestMovieIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CheckInMovieResponseDto

class CheckInApiClient(
    private val api: CheckinApi,
) : CheckInRemoteDataSource {
    override suspend fun postMovieCheckIn(movieId: TraktId): CheckInMovieResponseDto {
        val result = api.postCheckinMovie(
            postCheckinMovieRequest = PostCheckinMovieRequest(
                movie = PostCheckinMovieRequestMovie(
                    PostCheckinMovieRequestMovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = -1,
                    ),
                ),
            ),
        )

        return result.body()
    }

    override suspend fun deleteAll() {
        api.deleteCheckinDelete()
    }
}
