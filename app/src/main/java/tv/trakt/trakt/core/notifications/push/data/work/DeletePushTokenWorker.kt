package tv.trakt.trakt.core.notifications.push.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.notifications.push.usecases.DeletePushTokenUseCase
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val MAX_RETRY_ATTEMPTS = 3

private val BACKOFF_DELAY = 30.seconds

internal class DeletePushTokenWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val deletePushTokenUseCase: DeletePushTokenUseCase,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(appContext: Context) {
            val workRequest = OneTimeWorkRequestBuilder<DeletePushTokenWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY.toJavaDuration())
                .build()

            WorkManager.getInstance(appContext).enqueue(workRequest)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            deletePushTokenUseCase.deleteToken()
            Result.success()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            if (runAttemptCount + 1 < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }
    }
}
