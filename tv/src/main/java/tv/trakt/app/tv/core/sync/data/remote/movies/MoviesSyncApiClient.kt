package tv.trakt.app.tv.core.sync.data.remote.movies

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.apis.WatchedApi
import org.openapitools.client.models.PostCheckinMovieRequestMovieIds
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.networking.openapi.WatchedMovieDto
import tv.trakt.app.tv.networking.openapi.WatchlistMovieDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class MoviesSyncApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
    private val watchedApi: WatchedApi,
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
}
