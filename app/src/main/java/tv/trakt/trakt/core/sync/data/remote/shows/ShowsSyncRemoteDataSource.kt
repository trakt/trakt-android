package tv.trakt.trakt.core.sync.data.remote.shows

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ProgressShowDto

internal interface ShowsSyncRemoteDataSource {
    suspend fun getUpNext(
        limit: Int,
        page: Int,
        intent: String,
    ): List<ProgressShowDto>

    suspend fun addToWatched(
        showId: TraktId,
        watchedAt: String,
    )

    suspend fun removeAllFromHistory(showId: TraktId)

    suspend fun addToWatchlist(showId: TraktId)

    suspend fun removeFromWatchlist(showId: TraktId)

    suspend fun dropShow(showId: TraktId)

    suspend fun addToFavorites(showId: TraktId)

    suspend fun removeFromFavorites(showId: TraktId)
}
