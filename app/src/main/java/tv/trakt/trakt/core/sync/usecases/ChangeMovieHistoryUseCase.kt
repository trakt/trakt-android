package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource

internal class ChangeMovieHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
) {
    suspend fun addToHistory(movieId: TraktId) {
        remoteSource.addToHistory(
            movieId = movieId,
            watchedAt = nowUtcInstant(),
        )
    }
}
