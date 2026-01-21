package tv.trakt.trakt.core.sync.data.remote.movies

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.model.TraktId

internal interface MoviesSyncRemoteDataSource {
    suspend fun addToWatched(
        movieId: TraktId,
        watchedAt: String,
    ): PostSyncHistoryAdd200Response

    suspend fun removeAllFromHistory(movieId: TraktId)

    suspend fun removeSingleFromHistory(playId: Long)

    suspend fun addToWatchlist(movieId: TraktId)

    suspend fun removeFromWatchlist(movieId: TraktId)

    suspend fun addToFavorites(movieId: TraktId)

    suspend fun removeFromFavorites(movieId: TraktId)
}
