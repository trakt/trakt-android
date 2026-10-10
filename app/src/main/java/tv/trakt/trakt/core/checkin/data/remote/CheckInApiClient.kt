package tv.trakt.trakt.core.checkin.data.remote

import org.openapitools.client.apis.CheckinApi
import org.openapitools.client.infrastructure.HttpResponse
import org.openapitools.client.models.PostCheckinStart200Response
import org.openapitools.client.models.PostCheckinStartRequest
import org.openapitools.client.models.PostCheckinStartRequestOneOf1Movie
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOf1Show
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOf2Episode
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId

class CheckInApiClient(
    private val api: CheckinApi,
) : CheckInRemoteDataSource {
    override suspend fun postMovieCheckIn(movieId: TraktId): PostCheckinStart200Response? {
        val response = api.postCheckinStart(
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
        return response.bodyOrNull()
    }

    override suspend fun postEpisodeCheckIn(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): PostCheckinStart200Response? {
        val response = api.postCheckinStart(
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
        return response.bodyOrNull()
    }

    override suspend fun deleteAll() {
        api.deleteCheckinDelete()
    }

    // The body is only used for diagnostics, so a parsing failure must not fail the check-in.
    private suspend fun HttpResponse<PostCheckinStart200Response>.bodyOrNull(): PostCheckinStart200Response? {
        return try {
            body()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to parse check-in response")
            }
            null
        }
    }
}
