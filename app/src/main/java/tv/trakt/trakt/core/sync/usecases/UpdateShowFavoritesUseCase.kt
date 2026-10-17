package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.model.TraktId

internal class UpdateShowFavoritesUseCase(
    private val remoteSource: ShowsSyncRemoteSource,
) {
    suspend fun addToFavorites(showId: TraktId) {
        remoteSource.addToFavorites(
            showId = showId,
        )
    }

    suspend fun removeFromFavorites(showId: TraktId) {
        remoteSource.removeFromFavorites(
            showId = showId,
        )
    }
}
