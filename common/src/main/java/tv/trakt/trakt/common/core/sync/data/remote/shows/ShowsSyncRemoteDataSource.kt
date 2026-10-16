package tv.trakt.trakt.common.core.sync.data.remote.shows

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.common.networking.SyncAddHistoryResponseDto
import tv.trakt.trakt.common.networking.WatchlistShowDto

interface ShowsSyncRemoteDataSource {
    suspend fun getUpNext(
        limit: Int,
        page: Int,
        intent: String,
        sortBy: String?,
        sortHow: String?,
        filters: GlobalFilter? = null,
    ): List<ProgressShowDto>

    suspend fun addToWatched(
        showId: TraktId,
        watchedAt: String,
    ): SyncAddHistoryResponseDto

    suspend fun removeAllFromHistory(showId: TraktId)

    suspend fun addToWatchlist(showId: TraktId)

    suspend fun removeFromWatchlist(showId: TraktId)

    suspend fun dropShow(showId: TraktId)

    suspend fun hideRecommendation(showId: TraktId)

    suspend fun addToFavorites(showId: TraktId)

    suspend fun removeFromFavorites(showId: TraktId)

    suspend fun getWatchlist(
        sorting: Sorting = Sorting.Default,
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        hide: String? = null,
    ): List<WatchlistShowDto>

    suspend fun getWatched(): Map<String, Map<String, Map<String, List<String>>>>

    suspend fun getShowsPlexCollection(): Map<TraktId, Map<TraktId, Map<TraktId, String>>>

    suspend fun getEpisodesPlexCollection(): Map<TraktId, String>
}
