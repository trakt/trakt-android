package tv.trakt.trakt.common.core.user.data.remote.watchlist

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.WatchlistItemDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import tv.trakt.trakt.common.networking.WatchlistShowDto

interface UserWatchlistRemoteDataSource {
    suspend fun getWatchlistMinimal(): Pair<Set<TraktId>, Set<TraktId>>

    suspend fun getWatchlist(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sorting: Sorting? = null,
        filters: GlobalFilter?,
    ): List<WatchlistItemDto>

    suspend fun getWatchlistShows(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sorting: Sorting? = null,
        hide: String? = null,
        filters: GlobalFilter? = null,
    ): List<WatchlistShowDto>

    suspend fun getWatchlistMovies(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sorting: Sorting? = null,
        hide: String? = null,
        filters: GlobalFilter? = null,
    ): List<WatchlistMovieDto>
}
