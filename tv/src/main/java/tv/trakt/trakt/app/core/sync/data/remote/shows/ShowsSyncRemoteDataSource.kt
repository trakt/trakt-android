package tv.trakt.trakt.app.core.sync.data.remote.shows

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.common.networking.WatchedShowDto
import tv.trakt.trakt.common.networking.WatchlistShowDto
import java.time.ZonedDateTime

internal interface ShowsSyncRemoteDataSource {
    suspend fun getUpNextProgress(
        limit: Int,
        page: Int,
    ): List<ProgressShowDto>

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

    suspend fun getShowsPlexCollection(): Map<TraktId, Map<TraktId, TraktId>>

    suspend fun getEpisodesPlexCollection(): Map<TraktId, String>
}
