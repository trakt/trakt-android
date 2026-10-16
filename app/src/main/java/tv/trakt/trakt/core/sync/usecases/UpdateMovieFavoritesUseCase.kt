package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

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
