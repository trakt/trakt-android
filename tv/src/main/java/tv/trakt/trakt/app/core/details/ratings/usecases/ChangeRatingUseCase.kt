package tv.trakt.trakt.app.core.details.ratings.usecases

import timber.log.Timber
import tv.trakt.trakt.common.core.ratings.usecases.DeleteRatingUseCase
import tv.trakt.trakt.common.core.ratings.usecases.PostRatingUseCase
import tv.trakt.trakt.common.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

internal class ChangeRatingUseCase(
    private val postRatingUseCase: PostRatingUseCase,
    private val deleteRatingUseCase: DeleteRatingUseCase,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
    private val analytics: Analytics,
) {
    /**
     * Posts [rating] on the 1-10 scale, or removes the rating when [rating] is null.
     */
    suspend fun changeRating(
        mediaId: TraktId,
        mediaType: MediaType,
        rating: Int?,
    ) {
        if (rating == null) {
            deleteRatingUseCase.deleteRating(
                mediaId = mediaId,
                mediaType = mediaType,
            )
            analytics.ratings.logRatingRemove(mediaType = mediaType.value)
        } else {
            val isChange = hasLocalRating(mediaId, mediaType)
            postRatingUseCase.postRating(
                mediaId = mediaId,
                mediaType = mediaType,
                rating = rating,
            )
            if (isChange) {
                analytics.ratings.logRatingChange(rating = rating, mediaType = mediaType.value)
            } else {
                analytics.ratings.logRatingAdd(rating = rating, mediaType = mediaType.value)
            }
        }

        refreshLocalRatings(mediaType)
    }

    private suspend fun hasLocalRating(
        mediaId: TraktId,
        mediaType: MediaType,
    ): Boolean {
        val ratings = with(loadUserRatingsUseCase) {
            when (mediaType) {
                MediaType.Movie -> loadLocalMovies()
                MediaType.Show -> loadLocalShows()
                MediaType.Episode -> loadLocalEpisodes()
                MediaType.Season -> loadLocalSeasons()
            }
        }
        return ratings.containsKey(mediaId)
    }

    private suspend fun refreshLocalRatings(mediaType: MediaType) {
        try {
            with(loadUserRatingsUseCase) {
                when (mediaType) {
                    MediaType.Movie -> loadMovies()
                    MediaType.Show -> loadShows()
                    MediaType.Episode -> loadEpisodes()
                    MediaType.Season -> loadSeasons()
                }
            }
        } catch (error: Exception) {
            // The rating is already posted, so a failed refresh must not undo it on screen.
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }
}
