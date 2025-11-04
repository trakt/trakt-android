package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource

internal class UpdateMovieFavoritesUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
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
