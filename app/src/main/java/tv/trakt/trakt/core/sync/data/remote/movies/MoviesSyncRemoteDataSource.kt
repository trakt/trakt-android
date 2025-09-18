package tv.trakt.trakt.core.sync.data.remote.movies

import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal interface MoviesSyncRemoteDataSource {
    suspend fun addToHistory(
        movieId: TraktId,
        watchedAt: Instant,
    )

    suspend fun removeSingleFromHistory(playId: Long)

    suspend fun removeFromWatchlist(movieId: TraktId)
}
