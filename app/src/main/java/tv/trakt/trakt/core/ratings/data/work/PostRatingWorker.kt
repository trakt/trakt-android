package tv.trakt.trakt.core.ratings.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.errors.GlobalErrorsManager
import tv.trakt.trakt.common.helpers.extensions.isOnline
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.ratings.DeleteRatingUseCase
import tv.trakt.trakt.core.ratings.PostRatingUseCase
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import java.io.IOException
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

private const val MAX_RETRY_ATTEMPTS = 2

internal class PostRatingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    val sessionManager: SessionManager,
    val postRatingUseCase: PostRatingUseCase,
    val deleteRatingUseCase: DeleteRatingUseCase,
    val loadUserRatingUseCase: LoadUserRatingsUseCase,
    val ratingsUpdates: RatingsUpdates,
    val errorsManager: GlobalErrorsManager,
    val analytics: Analytics,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun scheduleOneTime(
            appContext: Context,
            mediaId: TraktId,
            mediaType: MediaType,
            rating: Int,
            source: RatingsUpdates.Source,
        ) {
            val workRequest = OneTimeWorkRequestBuilder<PostRatingWorker>()
                .setInputData(
                    Data.Builder()
                        .putInt("mediaId", mediaId.value)
                        .putString("mediaType", mediaType.name)
                        .putInt("rating", rating)
                        .putString("source", source.name)
                        .build(),
                )
                .setInitialDelay(300.milliseconds.toJavaDuration())
                .setBackoffCriteria(BackoffPolicy.LINEAR, 3, SECONDS)
                .build()

            WorkManager
                .getInstance(appContext)
                .enqueueUniqueWork(
                    "post_rating_${mediaId.value}_${mediaType.value}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
        }
    }

    override suspend fun doWork(): Result {
        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, cannot post rating")
            return Result.failure()
        }

        val mediaId = inputData.getInt("mediaId", -1)
        val mediaType = MediaType.entries.find { it.name == inputData.getString("mediaType") }
        val ratingValue = inputData.getInt("rating", -1)
        val source = RatingsUpdates.Source.entries
            .find { it.name == inputData.getString("source") }
            ?: RatingsUpdates.Source.Default

        if (mediaId == -1) {
            Timber.d("Invalid media ID, cannot post rating")
            return Result.failure()
        }

        if (mediaType == null) {
            Timber.d("Invalid media type, cannot post rating")
            return Result.failure()
        }

        if (ratingValue == -1) {
            Timber.d("No rating value provided, cannot post rating")
            return Result.failure()
        }

        if (!applicationContext.isOnline()) {
            Timber.d("No connection, cannot post rating")
            return failPosting(IOException("No network connection"))
        }

        try {
            withContext(Dispatchers.IO) {
                postRating(
                    mediaId = mediaId.toTraktId(),
                    mediaType = mediaType,
                    rating = ratingValue,
                )
            }
        } catch (error: Exception) {
            if (error is CancellationException) {
                return Result.failure()
            }
            Timber.recordError(error)

            if (runAttemptCount + 1 < MAX_RETRY_ATTEMPTS) {
                return Result.retry()
            }

            Timber.d("Max retry attempts reached, failing work")
            return failPosting(error)
        }

        try {
            withContext(Dispatchers.IO) {
                delay(200.milliseconds)
                when (mediaType) {
                    MediaType.Show -> loadUserRatingUseCase.loadShows()
                    MediaType.Movie -> loadUserRatingUseCase.loadMovies()
                    MediaType.Episode -> loadUserRatingUseCase.loadEpisodes()
                    MediaType.Season -> loadUserRatingUseCase.loadSeasons()
                }
            }
            ratingsUpdates.notifyUpdate(source)
        } catch (error: Exception) {
            if (error is CancellationException) {
                return Result.failure()
            }
            // The rating is posted, so screens keep their optimistic state instead of a stale refresh.
            Timber.recordError(error)
        }

        Timber.d("Successfully posted rating.")
        return Result.success()
    }

    private fun failPosting(error: Exception): Result {
        // The local ratings were never updated, so a refresh reverts optimistic UI updates.
        ratingsUpdates.notifyUpdate(RatingsUpdates.Source.Default)
        errorsManager.tryEmit(PostRatingException(error))
        return Result.failure()
    }

    private suspend fun postRating(
        mediaId: TraktId,
        mediaType: MediaType,
        rating: Int,
    ) {
        if (rating <= 0) {
            deleteRatingUseCase.deleteRating(
                mediaId = mediaId,
                mediaType = mediaType,
            )
            analytics.ratings.logRatingRemove(
                mediaType = mediaType.value,
            )
            return
        }

        postRatingUseCase.postRating(
            mediaId = mediaId,
            mediaType = mediaType,
            rating = rating,
        )
        analytics.ratings.logRatingAdd(
            rating = rating,
            mediaType = mediaType.value,
        )
    }
}
