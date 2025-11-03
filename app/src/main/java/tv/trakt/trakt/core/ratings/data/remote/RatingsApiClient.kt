package tv.trakt.trakt.core.ratings.data.remote

import org.openapitools.client.apis.RatingsApi
import org.openapitools.client.models.PostCheckinMovieRequestMovieIds
import org.openapitools.client.models.PostSyncRatingsAddRequest
import org.openapitools.client.models.PostSyncRatingsAddRequestMoviesInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class RatingsApiClient(
    private val ratingsApi: RatingsApi,
    private val cacheMarker: CacheMarkerProvider,
) : RatingsRemoteDataSource {
    override suspend fun postMovieRating(
        id: TraktId,
        rating: Int,
    ) {
        val request = PostSyncRatingsAddRequest(
            movies = listOf(
                PostSyncRatingsAddRequestMoviesInner(
                    rating = rating,
                    ids = PostCheckinMovieRequestMovieIds(
                        trakt = id.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                ),
            ),
        )
        ratingsApi.postSyncRatingsAdd(request)
        cacheMarker.invalidate()
    }
}
