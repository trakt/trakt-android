package tv.trakt.trakt.core.notifications.data.work

import android.app.Notification
import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.notifications.TraktNotificationChannel
import tv.trakt.trakt.resources.R
import java.time.Duration
import java.time.Instant
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
            channel: TraktNotificationChannel,
            mediaId: Int,
            mediaType: MediaType,
            title: String,
            content: String,
            targetDate: Instant,
        ) {
            val nowUtc = nowUtcInstant()
            val delay = Duration.between(nowUtc, targetDate)

            val workRequest = OneTimeWorkRequestBuilder<PostNotificationWorker>()
                .addTag(WORK_NOTIFICATION_TAG)
                .setInputData(
                    Data.Builder()
                        .putString("channel", channel.id)
                        .putString("title", title)
                        .putString("content", content)
                        .putInt("mediaId", mediaId)
                        .putString("mediaType", mediaType.value)
                        .build(),
                )
                .setInitialDelay(delay.toMillis(), MILLISECONDS)
                .build()

            with(WorkManager.getInstance(appContext)) {
                enqueueUniqueWork(
                    "${WORK_ID}-${mediaType.value}-$mediaId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
            }

            Timber.d("Notification scheduled: $mediaType - $mediaId at ${targetDate.toLocal()}.")
        }
    }

    override suspend fun doWork(): Result {
        val channel = inputData.getString("channel")
        val title = inputData.getString("title")
        val content = inputData.getString("content")
        val mediaId = inputData.getInt("mediaId", -1)
        val mediaType = inputData.getString("mediaType")

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
