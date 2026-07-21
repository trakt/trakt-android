package tv.trakt.trakt.app.core.sync.data.remote.shows

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.common.networking.WatchlistShowDto

internal interface ShowsSyncRemoteDataSource {
    suspend fun getUpNextProgress(
        limit: Int,
        page: Int,
        intent: String,
        sortBy: String?,
        sortHow: String?,
    ): List<ProgressShowDto>

    suspend fun getWatchlist(
        sorting: Sorting = Sorting.Default,
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        hide: String? = null,
    ): List<WatchlistShowDto>

    suspend fun getWatched(): Map<String, Map<String, Map<String, List<String>>>>

    suspend fun addToWatchlist(showId: TraktId)

    suspend fun removeFromWatchlist(showId: TraktId)

    suspend fun addToHistory(
        showId: TraktId,
        watchedAt: String,
    ): SyncAddHistoryResponseDto

    suspend fun removeFromHistory(showId: TraktId)

    suspend fun getShowsPlexCollection(): Map<TraktId, Map<TraktId, Map<TraktId, String>>>

    suspend fun getEpisodesPlexCollection(): Map<TraktId, String>
}
