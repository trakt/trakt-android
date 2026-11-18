package tv.trakt.trakt.core.notifications.push.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.notifications.push.usecases.SyncPushTokenUseCase
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val WORK_ID = "sync_push_token_work"
private const val MAX_RETRY_ATTEMPTS = 3

private val BACKOFF_DELAY = 30.seconds

internal class SyncPushTokenWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val syncPushTokenUseCase: SyncPushTokenUseCase,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(appContext: Context) {
            val workRequest = OneTimeWorkRequestBuilder<SyncPushTokenWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY.toJavaDuration())
                .build()

            // Replace, so a token rotated mid-run is fetched again instead of dropped.
            WorkManager.getInstance(appContext).enqueueUniqueWork(
                WORK_ID,
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            syncPushTokenUseCase.syncToken()
            Result.success()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            if (runAttemptCount + 1 < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }
    }
}
