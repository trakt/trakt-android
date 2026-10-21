package tv.trakt.trakt.core.ratings

import timber.log.Timber
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserRatingsLocalSource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

internal data class OrphanedRating(
    val id: TraktId,
    val type: MediaType,
)

internal enum class HistoryRemoval {
    SinglePlay,
    AllPlays,
    ;

    fun orphansRating(plays: Int): Boolean {
        return when (this) {
            SinglePlay -> plays == 1
            AllPlays -> plays > 0
        }
    }
}

/**
 * A rating left without any play in history is removed with the last play.
 * Ratings to remove are found from local progress, so they must be read before the history removal.
 */
internal class RemoveOrphanedRatingUseCase(
    private val progressLocalSource: UserProgressLocalSource,
    private val ratingsLocalSource: UserRatingsLocalSource,
    private val deleteRatingUseCase: DeleteRatingUseCase,
    private val dataChanges: DataChanges,
) {
    suspend fun findMovie(
        movieId: TraktId,
        removal: HistoryRemoval,
    ): OrphanedRating? {
        if (!ratingsLocalSource.containsMovie(movieId)) return null

        val plays = progressLocalSource.getMovies()
            .firstOrNull { it.mediaId == movieId }
            ?.plays
            ?.size
            ?: return null

        if (!removal.orphansRating(plays)) return null

        return OrphanedRating(movieId, MediaType.Movie)
    }

    suspend fun findShow(showId: TraktId): OrphanedRating? {
        if (!ratingsLocalSource.containsShow(showId)) return null
        if (!progressLocalSource.containsShow(showId)) return null

        return OrphanedRating(showId, MediaType.Show)
    }

    suspend fun findEpisode(
        episodeId: TraktId,
        removal: HistoryRemoval,
    ): OrphanedRating? {
        if (!ratingsLocalSource.containsEpisode(episodeId)) return null

        val plays = progressLocalSource.getShows()
            .asSequence()
            .flatMap { it.seasons }
            .flatMap { it.episodes }
            .firstOrNull { it.id == episodeId }
            ?.plays
            ?.size
            ?: return null

        if (!removal.orphansRating(plays)) return null

        return OrphanedRating(episodeId, MediaType.Episode)
    }

    suspend fun remove(rating: OrphanedRating?) {
        if (rating == null) return

        try {
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
}
