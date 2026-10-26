package tv.trakt.trakt.common.auth.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import tv.trakt.trakt.common.data.remote.AuthRemoteSource
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val KEY_ACCESS_TOKEN = "accessToken"
private const val MAX_RETRY_ATTEMPTS = 3

private val BACKOFF_DELAY = 3.seconds

class RevokeTokenWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val authRemoteSource: AuthRemoteSource,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(
            appContext: Context,
            accessToken: String,
        ) {
            val workRequest = OneTimeWorkRequestBuilder<RevokeTokenWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(KEY_ACCESS_TOKEN, accessToken)
                        .build(),
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY.toJavaDuration())
                .build()

            WorkManager
                .getInstance(appContext)
                .enqueue(workRequest)
        }
    }

    override suspend fun doWork(): Result {
        val accessToken = inputData.getString(KEY_ACCESS_TOKEN)
        if (accessToken.isNullOrBlank()) return Result.failure()

        return try {
            authRemoteSource.revokeToken(accessToken)
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (error: ClientRequestException) {
            Timber.w(error, "Token revoke rejected")
            Result.failure()
        } catch (error: Exception) {
            Timber.w(error, "Failed to revoke token")
            if (runAttemptCount + 1 < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }
    }
}
