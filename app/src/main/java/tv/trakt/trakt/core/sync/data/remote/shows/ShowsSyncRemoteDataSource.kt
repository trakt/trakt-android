package tv.trakt.trakt.core.sync.data.remote.shows

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.WatchlistShowDto

internal interface ShowsSyncRemoteDataSource {
    suspend fun getUpNext(
        limit: Int,
        page: Int,
    ): List<ProgressShowDto>

    suspend fun getWatchlist(
        sort: String = "rank",
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
    ): List<WatchlistShowDto>

    suspend fun dropShow(showId: TraktId)
}
