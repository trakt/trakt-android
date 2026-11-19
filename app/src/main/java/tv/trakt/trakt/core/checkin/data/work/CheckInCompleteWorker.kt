package tv.trakt.trakt.core.checkin.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import java.time.Duration
import java.time.Instant

private const val WORK_NAME = "check_in_complete"
private const val KEY_IS_MOVIE = "isMovie"
private const val MAX_RETRY_ATTEMPTS = 3

internal class CheckInCompleteWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val sessionManager: SessionManager,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val dataChanges: DataChanges,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun scheduleOneTime(
            appContext: Context,
            expiresAt: Instant,
            isMovie: Boolean,
        ) {
            val workRequest = OneTimeWorkRequestBuilder<CheckInCompleteWorker>()
                .setInputData(
                    Data.Builder()
                        .putBoolean(KEY_IS_MOVIE, isMovie)
                        .build(),
                )
                .setInitialDelay(Duration.between(Instant.now(), expiresAt).coerceAtLeast(Duration.ZERO))
                .build()

            WorkManager
                .getInstance(appContext)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    override suspend fun doWork(): Result {
        if (!sessionManager.isAuthenticated()) {
            return Result.success()
        }

        return try {
            when {
                inputData.getBoolean(KEY_IS_MOVIE, false) -> loadUserProgressUseCase.loadMoviesProgress()
                else -> loadUserProgressUseCase.loadShowsProgress()
            }
            dataChanges.notify(Kind.CheckIn)
            Timber.d("Check-in completed, progress reloaded.")
            Result.success()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }
    }
}
