package tv.trakt.trakt.app.core.sync.data.remote.movies

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ProgressMovieDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto

internal interface MoviesSyncRemoteDataSource {
    suspend fun getPlaybackProgress(
        limit: Int,
        page: Int,
    ): List<ProgressMovieDto>

    suspend fun getWatchlist(
        sorting: Sorting = Sorting.Default,
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        hide: String? = null,
    ): List<WatchlistMovieDto>

    suspend fun getWatched(): Map<String, List<String>>

    suspend fun addToWatchlist(movieId: TraktId)

    suspend fun removeFromWatchlist(movieId: TraktId)

    suspend fun addToHistory(
        movieId: TraktId,
        watchedAt: String,
    )

    suspend fun removeFromHistory(movieId: TraktId)

    suspend fun getPlexCollection(): Map<TraktId, String>
}
