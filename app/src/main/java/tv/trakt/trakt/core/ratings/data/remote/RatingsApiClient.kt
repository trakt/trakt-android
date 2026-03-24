package tv.trakt.trakt.core.ratings.data.remote

import org.openapitools.client.apis.RatingsApi
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import org.openapitools.client.models.PostSyncRatingsAddRequest
import org.openapitools.client.models.PostSyncRatingsAddRequestEpisodesInner
import org.openapitools.client.models.PostSyncRatingsAddRequestMoviesInner
import org.openapitools.client.models.PostSyncRatingsAddRequestShowsInner
import org.openapitools.client.models.PostSyncRatingsRemoveRequest
import org.openapitools.client.models.PostSyncRatingsRemoveRequestEpisodesInner
import org.openapitools.client.models.PostSyncRatingsRemoveRequestMoviesInner
import org.openapitools.client.models.PostSyncRatingsRemoveRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInnerIds
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
                    ids = PostCheckinStartRequestOneOf1MovieIds(
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

    override suspend fun postEpisodeRating(
        id: TraktId,
        rating: Int,
    ) {
        val request = PostSyncRatingsAddRequest(
            episodes = listOf(
                PostSyncRatingsAddRequestEpisodesInner(
                    rating = rating,
                    ids = PostUsersListsListAddRequestEpisodesInnerIds(
                        trakt = id.value,
                        tvdb = -1,
                    ),
                ),
            ),
        )
        ratingsApi.postSyncRatingsAdd(request)
        cacheMarker.invalidate()
    }

    override suspend fun postShowRating(
        id: TraktId,
        rating: Int,
    ) {
        val request = PostSyncRatingsAddRequest(
            shows = listOf(
                PostSyncRatingsAddRequestShowsInner(
                    rating = rating,
                    ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
                        trakt = id.value,
                        slug = null,
                        imdb = null,
                        tmdb = null,
                        tvdb = -1,
                    ),
                ),
            ),
        )
        ratingsApi.postSyncRatingsAdd(request)
        cacheMarker.invalidate()
    }

    override suspend fun deleteMovieRating(id: TraktId) {
        ratingsApi.postSyncRatingsRemove(
            PostSyncRatingsRemoveRequest(
                movies = listOf(
                    PostSyncRatingsRemoveRequestMoviesInner(
                        ids = PostCheckinStartRequestOneOf1MovieIds(
                            trakt = id.value,
                            slug = null,
                            imdb = null,
                            tmdb = 0,
                        ),
                    ),
                ),
            ),
        )
        cacheMarker.invalidate()
    }

    override suspend fun deleteEpisodeRating(id: TraktId) {
        ratingsApi.postSyncRatingsRemove(
            PostSyncRatingsRemoveRequest(
                episodes = listOf(
                    PostSyncRatingsRemoveRequestEpisodesInner(
                        ids = PostUsersListsListAddRequestEpisodesInnerIds(
                            trakt = id.value,
                            tvdb = -1,
                        ),
                    ),
                ),
            ),
        )
        cacheMarker.invalidate()
    }

    override suspend fun deleteShowRating(id: TraktId) {
        ratingsApi.postSyncRatingsRemove(
            PostSyncRatingsRemoveRequest(
                shows = listOf(
                    PostSyncRatingsRemoveRequestShowsInner(
                        ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
                            trakt = id.value,
                            slug = null,
                            imdb = null,
                            tmdb = null,
                            tvdb = -1,
                        ),
                    ),
                ),
            ),
        )
        cacheMarker.invalidate()
    }
}
