package tv.trakt.trakt.app.core.sync.data.local.movies

import tv.trakt.trakt.app.core.sync.model.WatchedMovie
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

internal interface MoviesSyncLocalDataSource {
    suspend fun saveWatchlist(
        moviesIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    )

    suspend fun removeWatchlist(
        moviesIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    )

    suspend fun getWatchlist(): Set<TraktId>?

    suspend fun saveWatched(
        movies: List<WatchedMovie>,
        timestamp: ZonedDateTime?,
    )

    suspend fun getWatched(): Map<TraktId, WatchedMovie>?

    suspend fun getWatchlistUpdatedAt(): ZonedDateTime?

    suspend fun getWatchedUpdatedAt(): ZonedDateTime?

    suspend fun clear()
}
