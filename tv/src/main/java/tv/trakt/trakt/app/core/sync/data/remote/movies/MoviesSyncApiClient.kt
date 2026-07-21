package tv.trakt.trakt.app.core.sync.data.remote.movies

import org.openapitools.client.apis.CollectionApi
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.apis.WatchedApi
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ProgressMovieDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class MoviesSyncApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
    private val watchedApi: WatchedApi,
    private val collectionApi: CollectionApi,
    private val cacheMarkerProvider: CacheMarkerProvider,
) : MoviesSyncRemoteDataSource {
    override suspend fun getPlaybackProgress(
        limit: Int,
        page: Int,
    ): List<ProgressMovieDto> {
        val response = syncApi.getSyncProgressMovies(
            extended = "full,cloud9,colors",
            page = page,
            limit = limit,
            startAt = null,
            endAt = null,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
            countries = null,
            certifications = null,
        )
        return response.body()
    }

    override suspend fun addToWatchlist(movieId: TraktId) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(movieId.value, null, null, 0),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistAdd(request)
        cacheMarkerProvider.invalidate()
    }

    override suspend fun removeFromWatchlist(movieId: TraktId) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(movieId.value, null, null, 0),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistRemove(request)
        cacheMarkerProvider.invalidate()
    }

    override suspend fun addToHistory(
        movieId: TraktId,
        watchedAt: String,
    ) {
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
        syncApi.postSyncHistoryAdd(request)
        cacheMarkerProvider.invalidate()
    }

    override suspend fun removeFromHistory(movieId: TraktId) {
        val request = PostSyncHistoryRemoveRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinStartRequestOneOf1MovieIds(movieId.value, null, null, 0),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncHistoryRemove(request)
        cacheMarkerProvider.invalidate()
    }

    override suspend fun getWatched(): Map<String, List<String>> {
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

    override suspend fun getWatchlist(
        sorting: Sorting,
        page: Int?,
        limit: Int?,
        extended: String?,
        hide: String?,
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

    /** Example:
     * {
     *     "696075": "2025-06-11T08:37:33.000Z"
     * }
     */
    override suspend fun getPlexCollection(): Map<TraktId, String> {
        val response = collectionApi.getSyncCollectionMinimalMovies(
            availableOn = "plex",
        )
        return response.body()
            .map { it.key.toInt().toTraktId() to it.value }
            .toMap()
    }
}
