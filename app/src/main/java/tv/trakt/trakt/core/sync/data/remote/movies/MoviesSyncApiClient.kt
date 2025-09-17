package tv.trakt.trakt.core.sync.data.remote.movies

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PostCheckinMovieRequestMovieIds
import org.openapitools.client.models.PostSyncHistoryRemoveRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import java.time.Instant

internal class MoviesSyncApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
) : MoviesSyncRemoteDataSource {
    override suspend fun addToHistory(
        movieId: TraktId,
        watchedAt: Instant,
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
                    watchedAt = watchedAt.toString(),
                ),
            ),
        )
        syncApi.postSyncHistoryAdd(request)
    }

    override suspend fun removeSingleFromHistory(playId: Long) {
        val request = PostSyncHistoryRemoveRequest(
            ids = listOf(playId),
        )
        syncApi.postSyncHistoryRemove(request)
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
