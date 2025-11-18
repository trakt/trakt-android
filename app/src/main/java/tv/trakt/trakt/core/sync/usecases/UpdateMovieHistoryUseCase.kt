package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class UpdateMovieHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
) {
    suspend fun addToWatched(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        remoteSource.addToWatched(
            movieId = movieId,
            watchedAt = watchedAt,
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
