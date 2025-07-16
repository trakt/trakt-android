package tv.trakt.app.tv.core.sync.data.remote.shows

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.networking.openapi.ProgressShowDto
import tv.trakt.app.tv.networking.openapi.SyncAddHistoryResponseDto
import tv.trakt.app.tv.networking.openapi.WatchedShowDto
import tv.trakt.app.tv.networking.openapi.WatchlistShowDto
import java.time.ZonedDateTime

internal interface ShowsSyncRemoteDataSource {
    suspend fun getUpNextProgress(limit: Int = 20): List<ProgressShowDto>

    suspend fun getWatchlist(
        sort: String = "rank",
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
    ): List<WatchlistShowDto>

    suspend fun getWatched(extended: String? = null): List<WatchedShowDto>

    suspend fun addToWatchlist(showId: TraktId)

    suspend fun removeFromWatchlist(showId: TraktId)

    suspend fun addToHistory(
        showId: TraktId,
        watchedAt: ZonedDateTime,
    ): SyncAddHistoryResponseDto
//
//    suspend fun removeFromHistory(showId: TraktId)
}
