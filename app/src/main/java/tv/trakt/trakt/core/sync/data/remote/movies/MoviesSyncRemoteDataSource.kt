package tv.trakt.trakt.core.sync.data.remote.movies

import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal interface MoviesSyncRemoteDataSource {
    suspend fun addToWatched(
        movieId: TraktId,
        watchedAt: Instant,
    )

    suspend fun removeAllFromHistory(movieId: TraktId)

    suspend fun removeSingleFromHistory(playId: Long)

    suspend fun addToWatchlist(movieId: TraktId)

    suspend fun removeFromWatchlist(movieId: TraktId)
}
