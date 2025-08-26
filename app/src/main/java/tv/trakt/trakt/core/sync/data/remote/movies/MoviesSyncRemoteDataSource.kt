package tv.trakt.trakt.core.sync.data.remote.movies

import tv.trakt.trakt.common.networking.WatchlistMovieDto

internal interface MoviesSyncRemoteDataSource {
    suspend fun getWatchlist(
        sort: String = "rank",
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
    ): List<WatchlistMovieDto>
}
