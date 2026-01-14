package tv.trakt.trakt.core.notifications.data.work

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.notifications.model.PostNotificationData
import tv.trakt.trakt.resources.R
import java.time.Duration
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val WORK_ID = "post_notif_work"
internal const val WORK_NOTIFICATION_TAG = "post_notif_work_tag"

internal class PostNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(
            appContext: Context,
            data: PostNotificationData,
        ) {
            val nowUtc = nowUtcInstant()
            val delay = Duration.between(nowUtc, data.targetDate)

            val workRequest = OneTimeWorkRequestBuilder<PostNotificationWorker>()
                .addTag(WORK_NOTIFICATION_TAG)
                .setInputData(data.toInputData())
                .setInitialDelay(delay.toMillis(), MILLISECONDS)
                .build()

            with(WorkManager.getInstance(appContext)) {
                enqueueUniqueWork(
                    "${WORK_ID}-${data.mediaType.value}-${data.mediaId}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
            }

            Timber.d("Notification scheduled: ${data.mediaType} - ${data.mediaId} at ${data.targetDate.toLocal()}.")
        }
    }

    override suspend fun doWork(): Result {
        val channel = inputData.getString(PostNotificationData.CHANNEL)
        val title = inputData.getString(PostNotificationData.TITLE)
        val content = inputData.getString(PostNotificationData.CONTENT)
        val mediaId = inputData.getInt(PostNotificationData.MEDIA_ID, -1)
        val mediaType = inputData.getString(PostNotificationData.MEDIA_TYPE)

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(applicationContext, POST_NOTIFICATIONS) == PERMISSION_GRANTED
        } else {
            // On Android 12 and below, notification permission is granted by default
            true
        }

        if (!hasPermission) {
            Timber.e("Missing POST_NOTIFICATIONS permission for notification")
            return Result.failure()
        }

        if (mediaId == -1) {
            Timber.e("Invalid media ID for notification")
            return Result.failure()
        }

        val notificationId = "${WORK_ID}-$mediaType-$mediaId".hashCode()
        val notification = Notification
            .Builder(applicationContext, channel)
            .setSmallIcon(R.drawable.ic_trakt_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setColor(Purple500.toArgb())

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                notificationId,
                notification.build(),
            )

        return Result.success()
    }
}
