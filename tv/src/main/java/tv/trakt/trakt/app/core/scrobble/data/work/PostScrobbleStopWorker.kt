package tv.trakt.trakt.app.core.scrobble.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import org.openapitools.client.apis.ScrobbleApi
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostScrobbleStartRequest
import org.openapitools.client.models.PostScrobbleStartRequestOneOf1Episode
import org.openapitools.client.models.PostScrobbleStartRequestOneOfMovie
import org.openapitools.client.models.PostUsersListsListAddRequestEpisodesInnerIds
import timber.log.Timber
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates.Source.SCROBBLE_STOP_WORKER
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.util.concurrent.TimeUnit.SECONDS

private const val MAX_RETRY_ATTEMPTS = 1

internal class PostScrobbleStopWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val sessionManager: SessionManager,
    private val scrobbleApi: ScrobbleApi,
    private val scrobbleUpdates: ScrobbleUpdates,
    private val cacheMarker: CacheMarkerProvider,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun scheduleOneTime(
            appContext: Context,
            mediaId: TraktId,
            mediaType: MediaType,
            progress: Float,
        ) {
            val workRequest = OneTimeWorkRequestBuilder<PostScrobbleStopWorker>()
                .setInputData(
                    Data.Builder()
                        .putInt("mediaId", mediaId.value)
                        .putString("mediaType", mediaType.name)
                        .putFloat("progress", progress)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 3, SECONDS)
                .build()

            WorkManager
                .getInstance(appContext)
                .enqueueUniqueWork(
                    "post_scrobble_stop",
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
        }
    }

    override suspend fun doWork(): Result {
        try {
            if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                Timber.d("Max retry attempts reached, failing work")
                return Result.failure()
            }

            if (!sessionManager.isAuthenticated()) {
                Timber.d("Not authenticated, cannot post scrobble stop")
                return Result.failure()
            }

            val mediaId = inputData.getInt("mediaId", -1)
            val mediaType = inputData.getString("mediaType")?.let {
                MediaType.valueOf(it)
            }
            val progress = inputData.getFloat("progress", 0f)

            if (mediaId == -1) {
                Timber.d("Invalid media ID, cannot post scrobble stop")
                return Result.failure()
            }

            if (mediaType == null) {
                Timber.d("Invalid media type, cannot post scrobble stop")
                return Result.failure()
            }

            if (mediaType == MediaType.Movie) {
                Timber.d("Posting scrobble stop for movie ID $mediaId with progress $progress")
                scrobbleApi.postScrobbleStop(
                    postScrobbleStartRequest = PostScrobbleStartRequest(
                        progress = progress.coerceIn(0f, 100f),
                        movie = PostScrobbleStartRequestOneOfMovie(
                            ids = PostCheckinStartRequestOneOf1MovieIds(
                                trakt = mediaId,
                                tmdb = 0,
                                imdb = null,
                                slug = null,
                            ),
                            title = "",
                            year = 0,
                        ),
                    ),
                )
                cacheMarker.invalidate()
                scrobbleUpdates.notifyUpdate(SCROBBLE_STOP_WORKER)
            }

            if (mediaType == MediaType.Episode) {
                Timber.d("Posting scrobble stop for episode ID $mediaId with progress $progress")
                scrobbleApi.postScrobbleStop(
                    postScrobbleStartRequest = PostScrobbleStartRequest(
                        progress = progress.coerceIn(0f, 100f),
                        episode = PostScrobbleStartRequestOneOf1Episode(
                            ids = PostUsersListsListAddRequestEpisodesInnerIds(
                                trakt = mediaId,
                                tvdb = 0,
                            ),
                        ),
                    ),
                )
                cacheMarker.invalidate()
                scrobbleUpdates.notifyUpdate(SCROBBLE_STOP_WORKER)
            }
        } catch (error: Exception) {
            if (error is CancellationException) {
                return Result.failure()
            }
            Timber.e(error)
            return Result.retry()
        }

        Timber.d("Successfully posted scrobble stop.")
        return Result.success()
    }
}
