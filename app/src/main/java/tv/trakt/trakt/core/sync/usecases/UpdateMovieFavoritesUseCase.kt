package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.model.TraktId

internal class UpdateMovieFavoritesUseCase(
    private val remoteSource: MoviesSyncRemoteSource,
) {
    suspend fun addToFavorites(movieId: TraktId) {
        remoteSource.addToFavorites(
            movieId = movieId,
        )
    }

    suspend fun removeFromFavorites(movieId: TraktId) {
        remoteSource.removeFromFavorites(
            movieId = movieId,
        )
    }
}
