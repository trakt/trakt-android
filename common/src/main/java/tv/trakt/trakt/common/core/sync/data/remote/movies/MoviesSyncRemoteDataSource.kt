package tv.trakt.trakt.common.core.sync.data.remote.movies

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ProgressMovieDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto

interface MoviesSyncRemoteDataSource {
    suspend fun getPlaybackProgress(
        limit: Int,
        page: Int,
        filters: GlobalFilter? = null,
    ): List<ProgressMovieDto>

    suspend fun addToWatched(
        movieId: TraktId,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response

    suspend fun removeAllFromHistory(movieId: TraktId)

    suspend fun removeSingleFromHistory(playId: Long)

    suspend fun addToWatchlist(movieId: TraktId)

    suspend fun removeFromWatchlist(movieId: TraktId)

    suspend fun hideRecommendation(movieId: TraktId)

    suspend fun addToFavorites(movieId: TraktId)

    suspend fun removeFromFavorites(movieId: TraktId)

    suspend fun getWatchlist(
        sorting: Sorting = Sorting.Default,
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        hide: String? = null,
    ): List<WatchlistMovieDto>

    suspend fun getWatched(): Map<String, List<String>>

    suspend fun getPlexCollection(): Map<TraktId, String>
}
