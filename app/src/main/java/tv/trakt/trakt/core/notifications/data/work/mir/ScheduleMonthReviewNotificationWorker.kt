package tv.trakt.trakt.core.notifications.data.work.mir

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.notifications.usecases.EnableNotificationsUseCase

private const val WORK_ID = "schedule_month_review_notification_work"
internal const val WORK_MONTH_REVIEW_TAG = "month_review_notif_work_tag"

internal class ScheduleMonthReviewNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val sessionManager: SessionManager,
    private val enableNotificationsUseCase: EnableNotificationsUseCase,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(appContext: Context) {
            val workRequest = OneTimeWorkRequestBuilder<ScheduleMonthReviewNotificationWorker>()
                .addTag(WORK_MONTH_REVIEW_TAG)
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                WORK_ID,
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )
        }

        fun clear(appContext: Context) {
            WorkManager.getInstance(appContext).cancelAllWorkByTag(WORK_MONTH_REVIEW_TAG)
        }
    }

    override suspend fun doWork(): Result {
        try {
            if (!enableNotificationsUseCase.isNotificationsEnabled()) {
                Timber.d("Notifications are disabled, skipping month in review notification.")
                return Result.failure()
            }

            val user = sessionManager.getProfile()
            val userSlug = user?.ids?.slug?.value
            if (user?.isAnyVip != true || userSlug.isNullOrBlank()) {
                Timber.d("User is not VIP, skipping month in review notification.")
                return Result.failure()
            }

            PostMonthReviewNotificationWorker.schedule(
                appContext = applicationContext,
                userSlug = userSlug,
            )
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            return Result.failure()
        }

        return Result.success()
    }
}
