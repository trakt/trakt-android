package tv.trakt.trakt.core.sync.usecases

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.ratings.RemoveOrphanedRatingUseCase

internal class UpdateMovieHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteSource,
    private val orphanedRatings: RemoveOrphanedRatingUseCase,
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
        val orphanedRating = orphanedRatings.findMovie(movieId)
        remoteSource.removeAllFromHistory(
            movieId = movieId,
        )
        orphanedRatings.remove(orphanedRating)
    }

    suspend fun removePlayFromHistory(
        playId: Long,
        movieId: TraktId,
    ) {
        val orphanedRating = orphanedRatings.findMovie(movieId)
        remoteSource.removeSingleFromHistory(
            playId = playId,
        )
        orphanedRatings.remove(orphanedRating)
    }
}
