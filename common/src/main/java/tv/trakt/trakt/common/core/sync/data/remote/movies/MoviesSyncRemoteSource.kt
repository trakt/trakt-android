package tv.trakt.trakt.common.core.sync.data.remote.movies

import org.openapitools.client.apis.CollectionApi
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.apis.WatchedApi
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostSyncFavoritesAddRequest
import org.openapitools.client.models.PostSyncHistoryAdd200Response
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostSyncRatingsRemoveRequestMoviesInner
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ProgressMovieDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class MoviesSyncRemoteSource(
    private val syncApi: SyncApi,
    private val recommendationsApi: RecommendationsApi,
    private val cacheMarker: CacheMarkerProvider,
    private val usersApi: UsersApi,
    private val watchedApi: WatchedApi,
    private val collectionApi: CollectionApi,
) {
    suspend fun getPlaybackProgress(
        limit: Int,
        page: Int,
        filters: GlobalFilter? = null,
    ): List<ProgressMovieDto> {
        val response = syncApi.getSyncProgressMovies(
            extended = "full,images,colors",
            page = page,
            limit = limit,
            startAt = null,
            endAt = null,
        )
        return response.body()
    }

    suspend fun addToWatched(
        movieId: TraktId,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                    title = "",
                    year = 0,
                    watchedAt = watchedAt,
                ),
            ),
        )
        val response = syncApi.postSyncHistoryAdd(request)
        cacheMarker.invalidate()

        return response.body()
    }

    suspend fun removeSingleFromHistory(playId: Long) {
        val request = PostSyncHistoryRemoveRequest(
            ids = listOf(playId),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

    suspend fun removeAllFromHistory(movieId: TraktId) {
        val request = PostSyncHistoryRemoveRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarker.invalidate()
    }

    suspend fun addToWatchlist(movieId: TraktId) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistAdd(request)
        cacheMarker.invalidate()
    }

    suspend fun removeFromWatchlist(movieId: TraktId) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistRemove(request)
        cacheMarker.invalidate()
    }

    suspend fun hideRecommendation(movieId: TraktId) {
        recommendationsApi.deleteRecommendationsMoviesHide(id = movieId.value.toString())
        cacheMarker.invalidate()
    }

    suspend fun addToFavorites(movieId: TraktId) {
        val request = PostSyncFavoritesAddRequest(
            movies = listOf(
                PostSyncRatingsRemoveRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                ),
            ),
        )
        syncApi.postSyncFavoritesAdd(request)
        cacheMarker.invalidate()
    }

    suspend fun removeFromFavorites(movieId: TraktId) {
        val request = PostSyncFavoritesAddRequest(
            movies = listOf(
                PostSyncRatingsRemoveRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                ),
            ),
        )
        syncApi.postSyncFavoritesRemove(request)
        cacheMarker.invalidate()
    }

    suspend fun getWatchlist(
        sorting: Sorting = Sorting.Default,
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        hide: String? = null,
    ): List<WatchlistMovieDto> {
        val response = usersApi.getUsersWatchlistMovies(
            id = "me",
            sort = "",
            extended = extended,
            page = page,
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            subgenres = null,
            hide = hide,
            runtimes = null,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            countries = null,
            certifications = null,
        )

        return response.body()
    }

    suspend fun getWatched(): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        var page = 1

        while (true) {
            val pageResponse = watchedApi.getUsersWatchedMinimalMovies(
                id = "me",
                extended = "min",
                page = page,
                limit = 100,
            ).body()

            if (pageResponse.isEmpty()) {
                break
            }

            result.putAll(pageResponse)
            page++
        }

        return result
    }

    /** Example:
     * {
     *     "696075": "2025-06-11T08:37:33.000Z"
     * }
     */
    suspend fun getPlexCollection(): Map<TraktId, String> {
        val response = collectionApi.getSyncCollectionMinimalMovies(
            availableOn = "digital",
        )
        return response.body()
            .map { it.key.toInt().toTraktId() to it.value }
            .toMap()
    }
}
