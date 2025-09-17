package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource

internal class UpdateMovieHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
) {
    suspend fun addToHistory(movieId: TraktId) {
        remoteSource.addToHistory(
            movieId = movieId,
            watchedAt = nowUtcInstant(),
        )
    }

    suspend fun removePlayFromHistory(playId: Long) {
        remoteSource.removeSingleFromHistory(
            playId = playId,
        )
    }
}
