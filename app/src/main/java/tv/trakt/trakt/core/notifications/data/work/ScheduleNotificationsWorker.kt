package tv.trakt.trakt.core.notifications.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.core.main.model.MediaMode

private const val WORK_ID = "schedule_notifications_work"

internal class ScheduleNotificationsWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    val sessionManager: SessionManager,
    val getUpcomingUseCase: GetUpcomingUseCase,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(appContext: Context) {
            val workRequest = OneTimeWorkRequestBuilder<ScheduleNotificationsWorker>()
                .build()

            WorkManager
                .getInstance(appContext)
                .enqueueUniqueWork(
                    WORK_ID,
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
        }
    }

    override suspend fun doWork(): Result {
        try {
            if (!sessionManager.isAuthenticated()) {
                Timber.d("Not authenticated, skipping.")
                return Result.failure()
            }

            var upcomingItems = getUpcomingUseCase.getLocalUpcoming(MediaMode.MEDIA)
            if (upcomingItems.isEmpty()) {
                upcomingItems = getUpcomingUseCase.getUpcoming(MediaMode.MEDIA)
            }

            // TODO : Schedule notifications for upcomingItems
        } catch (error: Exception) {
            if (error is CancellationException) {
                return Result.failure()
            }
            Timber.recordError(error)
            return Result.failure()
        }

        Timber.d("Successfully scheduled notifications.")
        return Result.success()
    }
}
