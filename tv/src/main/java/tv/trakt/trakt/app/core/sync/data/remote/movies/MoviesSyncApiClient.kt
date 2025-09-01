package tv.trakt.trakt.app.core.sync.data.remote.movies

import org.openapitools.client.apis.CollectionApi
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.apis.WatchedApi
import org.openapitools.client.models.PostCheckinMovieRequestMovieIds
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.WatchedMovieDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class MoviesSyncApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
    private val watchedApi: WatchedApi,
    private val collectionApi: CollectionApi,
) : MoviesSyncRemoteDataSource {
    override suspend fun addToWatchlist(movieId: TraktId) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinMovieRequestMovieIds(movieId.value, null, null, 0),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistAdd(request)
    }

    override suspend fun removeFromWatchlist(movieId: TraktId) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinMovieRequestMovieIds(movieId.value, null, null, 0),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncWatchlistRemove(request)
    }

    override suspend fun addToHistory(
        movieId: TraktId,
        watchedAt: ZonedDateTime,
    ) {
        val request = PostUsersListsListAddRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinMovieRequestMovieIds(
                        trakt = movieId.value,
                        slug = null,
                        imdb = null,
                        tmdb = 0,
                    ),
                    title = "",
                    year = 0,
                    watchedAt = watchedAt.format(DateTimeFormatter.ISO_INSTANT),
                ),
            ),
        )
        syncApi.postSyncHistoryAdd(request)
    }

    override suspend fun removeFromHistory(movieId: TraktId) {
        val request = PostSyncHistoryRemoveRequest(
            movies = listOf(
                PostUsersListsListAddRequestMoviesInner(
                    ids = PostCheckinMovieRequestMovieIds(movieId.value, null, null, 0),
                    title = "",
                    year = 0,
                ),
            ),
        )
        syncApi.postSyncHistoryRemove(request)
    }

    override suspend fun getWatched(extended: String?): List<WatchedMovieDto> {
        val response = watchedApi.getUsersWatchedMovies(
            id = "me",
        )
        return response.body()
    }

    override suspend fun getWatchlist(
        sort: String,
        page: Int?,
        limit: Int?,
        extended: String?,
    ): List<WatchlistMovieDto> {
        val response = usersApi.getUsersWatchlistMovies(
            id = "me",
            sort = sort,
            extended = extended,
            page = page,
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    /** Example:
     * {
     *     "696075": "2025-06-11T08:37:33.000Z"
     * }
     */
    override suspend fun getMoviesPlexCollection(): Map<TraktId, String> {
        val response = collectionApi.getSyncCollectionMinimalMovies(
            extended = "min",
            availableOn = "plex",
        )
        return response.body()
            .map { it.key.toInt().toTraktId() to it.value }
            .toMap()
    }
}
