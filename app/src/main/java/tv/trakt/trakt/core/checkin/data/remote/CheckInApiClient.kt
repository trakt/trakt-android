package tv.trakt.trakt.core.checkin.data.remote

import org.openapitools.client.apis.CheckinApi
import org.openapitools.client.models.PostCheckinEpisodeRequest
import org.openapitools.client.models.PostCheckinEpisodeRequestEp
import org.openapitools.client.models.PostCheckinMovieRequest
import org.openapitools.client.models.PostCheckinMovieRequestMovie
import org.openapitools.client.models.PostCheckinMovieRequestMovieIds
import tv.trakt.trakt.common.model.TraktId

class CheckInApiClient(
    private val api: CheckinApi,
) : CheckInRemoteDataSource {
    override suspend fun postMovieCheckIn(movieId: TraktId) {
        api.postCheckinMovie(
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
    }

    override suspend fun postEpisodeCheckIn(
        showId: TraktId,
        season: Int,
        episode: Int,
    ) {
        api.postCheckinEpisode(
            postCheckinMovieRequest = PostCheckinEpisodeRequest(
                show = PostCheckinMovieRequestMovie(
                    ids = PostCheckinMovieRequestMovieIds(
                        trakt = showId.value,
                        slug = null,
                        imdb = null,
                        tmdb = -1,
                    ),
                ),
                episode = PostCheckinEpisodeRequestEp(
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
