package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource

internal class UpdateMovieHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
) {
    suspend fun addToWatched(movieId: TraktId) {
        remoteSource.addToWatched(
            movieId = movieId,
            watchedAt = nowUtcInstant(),
        )
    }

    suspend fun removeAllFromHistory(movieId: TraktId) {
        remoteSource.removeAllFromHistory(
            movieId = movieId,
        )
    }

    suspend fun removePlayFromHistory(playId: Long) {
        remoteSource.removeSingleFromHistory(
            playId = playId,
        )
    }
}
