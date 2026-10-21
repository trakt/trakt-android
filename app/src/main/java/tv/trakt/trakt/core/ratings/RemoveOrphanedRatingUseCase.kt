package tv.trakt.trakt.core.ratings

import timber.log.Timber
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserRatingsLocalSource
import tv.trakt.trakt.common.data.remote.UserHistoryRemoteSource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination

internal data class OrphanedRating(
    val id: TraktId,
    val type: MediaType,
)

/**
 * A rating left without any play in history is removed with the last play.
 * Candidates come from local ratings and progress read before the history removal. Local progress
 * can be stale, so the rating is deleted only after the live history confirms no play is left.
 */
internal class RemoveOrphanedRatingUseCase(
    private val progressLocalSource: UserProgressLocalSource,
    private val ratingsLocalSource: UserRatingsLocalSource,
    private val historyRemoteSource: UserHistoryRemoteSource,
    private val deleteRatingUseCase: DeleteRatingUseCase,
    private val dataChanges: DataChanges,
) {
    suspend fun findMovie(movieId: TraktId): OrphanedRating? {
        if (!ratingsLocalSource.containsMovie(movieId)) return null
        if (!progressLocalSource.containsMovie(movieId)) return null

        return OrphanedRating(movieId, MediaType.Movie)
    }

    suspend fun findShow(showId: TraktId): OrphanedRating? {
        if (!ratingsLocalSource.containsShow(showId)) return null
        if (!progressLocalSource.containsShow(showId)) return null

        return OrphanedRating(showId, MediaType.Show)
    }

    suspend fun findEpisode(episodeId: TraktId): OrphanedRating? {
        if (!ratingsLocalSource.containsEpisode(episodeId)) return null

        val isWatched = progressLocalSource.getShows()
            .asSequence()
            .flatMap { it.seasons }
            .flatMap { it.episodes }
            .any { it.id == episodeId && it.plays.isNotEmpty() }

        if (!isWatched) return null

        return OrphanedRating(episodeId, MediaType.Episode)
    }

    suspend fun remove(rating: OrphanedRating?) {
        if (rating == null) return

        try {
            if (hasRemainingPlays(rating)) return

            deleteRatingUseCase.deleteRating(
                mediaId = rating.id,
                mediaType = rating.type,
            )

            val ids = setOf(rating.id)
            when (rating.type) {
                MediaType.Movie -> ratingsLocalSource.removeMovies(ids, notify = true)
                MediaType.Show -> ratingsLocalSource.removeShows(ids, notify = true)
                MediaType.Episode -> ratingsLocalSource.removeEpisodes(ids, notify = true)
                MediaType.Season -> ratingsLocalSource.removeSeasons(ids, notify = true)
            }

            dataChanges.notify(Kind.Ratings)
        } catch (error: Exception) {
            // The history removal already succeeded, so a failed cleanup must not surface as its error.
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private suspend fun hasRemainingPlays(rating: OrphanedRating): Boolean {
        val history = when (rating.type) {
            MediaType.Movie -> historyRemoteSource.getMovieHistory(rating.id, limit = 1)
            MediaType.Show -> historyRemoteSource.getShowHistory(rating.id, Pagination(limit = 1))
            MediaType.Episode -> historyRemoteSource.getEpisodeHistory(rating.id, limit = 1)
            MediaType.Season -> return true
        }

        return history.isNotEmpty()
    }
}
