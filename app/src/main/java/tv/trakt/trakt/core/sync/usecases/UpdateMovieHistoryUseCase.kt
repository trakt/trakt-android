package tv.trakt.trakt.core.sync.usecases

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.core.sync.data.remote.movies.MoviesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.TraktId

internal class UpdateMovieHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteSource,
) {
    suspend fun addToWatched(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
    ): PostSyncHistoryAdd200Response {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        return remoteSource.addToWatched(
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
