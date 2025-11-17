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
import kotlinx.coroutines.withContext
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.ratings.PostRatingUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import java.util.concurrent.TimeUnit.SECONDS

private const val MAX_RETRY_ATTEMPTS = 2

internal class PostRatingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    val sessionManager: SessionManager,
    val postRatingUseCase: PostRatingUseCase,
    val loadUserRatingUseCase: LoadUserRatingsUseCase,
    val analytics: Analytics,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun scheduleOneTime(
            appContext: Context,
            mediaId: TraktId,
            mediaType: MediaType,
            rating: Int,
        ) {
            val workRequest = OneTimeWorkRequestBuilder<PostRatingWorker>()
                .setInputData(
                    Data.Builder()
                        .putInt("mediaId", mediaId.value)
                        .putString("mediaType", mediaType.name)
                        .putInt("rating", rating)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 3, SECONDS)
                .build()

            WorkManager
                .getInstance(appContext)
                .enqueueUniqueWork(
                    "post_rating_${mediaId}_${mediaType.value}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
        }
    }

    override suspend fun doWork(): Result {
        try {
            if (!sessionManager.isAuthenticated()) {
                Timber.d("Not authenticated, cannot post rating")
                return Result.failure()
            }

            if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                Timber.d("Max retry attempts reached, failing work")
                return Result.failure()
            }

            val mediaId = inputData.getInt("mediaId", -1)
            val mediaType = inputData.getString("mediaType")?.let {
                MediaType.valueOf(it)
            }
            val ratingValue = inputData.getInt("rating", -1)

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

            withContext(Dispatchers.IO) {
                postRatingUseCase.postRating(
                    mediaId = mediaId.toTraktId(),
                    mediaType = mediaType,
                    rating = ratingValue,
                )

                analytics.ratings.logRatingAdd(
                    rating = ratingValue,
                    mediaType = mediaType.value,
                )

                when (mediaType) {
                    MediaType.SHOW -> loadUserRatingUseCase.loadShows()
                    MediaType.MOVIE -> loadUserRatingUseCase.loadMovies()
                    MediaType.EPISODE -> loadUserRatingUseCase.loadEpisodes()
                    else -> throw IllegalStateException("Rating is not supported")
                }
            }
        } catch (error: Exception) {
            if (error is CancellationException) {
                return Result.failure()
            }
            Timber.recordError(error)
            return Result.retry()
        }

        Timber.d("Successfully posted rating.")
        return Result.success()
    }
}
