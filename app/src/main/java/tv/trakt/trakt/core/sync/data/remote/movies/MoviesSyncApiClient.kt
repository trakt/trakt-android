package tv.trakt.trakt.core.sync.data.remote.movies

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.networking.WatchlistMovieDto

internal class MoviesSyncApiClient(
    private val usersApi: UsersApi,
) : MoviesSyncRemoteDataSource {
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
