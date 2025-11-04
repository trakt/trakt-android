package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class UpdateShowFavoritesUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
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
