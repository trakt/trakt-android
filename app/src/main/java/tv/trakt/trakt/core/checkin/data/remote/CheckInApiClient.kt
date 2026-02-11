package tv.trakt.trakt.core.checkin.data.remote

import org.openapitools.client.apis.CheckinApi
import org.openapitools.client.models.PostCheckinStartRequest
import org.openapitools.client.models.PostCheckinStartRequestOneOf1Movie
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOf1Show
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOf2Episode
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import tv.trakt.trakt.common.model.TraktId

class CheckInApiClient(
    private val api: CheckinApi,
) : CheckInRemoteDataSource {
    override suspend fun postMovieCheckIn(movieId: TraktId) {
        api.postCheckinStart(
            postCheckinStartRequest = PostCheckinStartRequest(
                movie = PostCheckinStartRequestOneOf1Movie(
                    PostCheckinStartRequestOneOf1MovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = -1,
                    ),
                ),
            ),
        )
    }

    override suspend fun postEpisodeCheckIn(
        showId: TraktId,
        season: Int,
        episode: Int,
    ) {
        api.postCheckinStart(
            postCheckinStartRequest = PostCheckinStartRequest(
                show = PostCheckinStartRequestOneOfOneOf1Show(
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
                        trakt = showId.value,
                        tvdb = -1,
                        imdb = null,
                        tmdb = null,
                        slug = null,
                    ),
                ),
                episode = PostCheckinStartRequestOneOfOneOf2Episode(
                    season = season,
                    number = episode,
                ),
            ),
        )
    }

    override suspend fun deleteAll() {
        api.deleteCheckinDelete()
    }
}
