package tv.trakt.trakt.core.notifications.data.work

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.notifications.model.NotificationIntentExtras
import tv.trakt.trakt.core.notifications.model.PostNotificationData
import tv.trakt.trakt.core.notifications.usecases.EnableNotificationsUseCase
import tv.trakt.trakt.resources.R
import java.time.Duration
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val MAIN_ACTIVITY_PATH = "tv.trakt.trakt.MainActivity"
private const val WORK_ID = "post_notif_work"
internal const val WORK_NOTIFICATION_TAG = "post_notif_work_tag"

internal const val INTENT_NOTIFICATION_EXTRAS = "NOTIFICATION_INTENT_EXTRAS"

internal class PostNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    val enableNotificationsUseCase: EnableNotificationsUseCase,
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
        val mediaImage = inputData.getString(PostNotificationData.MEDIA_IMAGE)

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

        if (!enableNotificationsUseCase.isNotificationsEnabled()) {
            Timber.d("Notifications are disabled, skipping notification.")
            return Result.failure()
        }

        if (mediaId == -1) {
            Timber.e("Invalid media ID for notification")
            return Result.failure()
        }

        val notificationId = "${WORK_ID}-$mediaType-$mediaId".hashCode()
        val notification = Notification
            .Builder(applicationContext, channel)
            .setContentIntent(createNotificationIntent())
            .setSmallIcon(R.drawable.ic_trakt_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setColor(Purple500.toArgb())

        if (!mediaImage.isNullOrBlank()) {
            try {
                val imageLoader = ImageLoader(applicationContext)
                val request = ImageRequest.Builder(applicationContext)
                    .data(mediaImage)
                    .allowHardware(false) // Disable hardware bitmaps for notifications
                    .build()

                val result = imageLoader.execute(request)
                val bitmap = (result.image as? BitmapDrawable)?.bitmap
                    ?: result.image?.toBitmap()

                if (bitmap != null) {
                    notification.setLargeIcon(bitmap)
                }
            } catch (error: Exception) {
                Timber.e(error, "Failed to load notification image")
            }
        }

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                notificationId,
                notification.build(),
            )

        return Result.success()
    }

    private fun createNotificationIntent(): PendingIntent {
        val targetClass = Class.forName(MAIN_ACTIVITY_PATH)

        val mediaId = inputData.getInt(PostNotificationData.MEDIA_ID, -1)
        val mediaType = inputData.getString(PostNotificationData.MEDIA_TYPE)

        val extraId = inputData.getInt(PostNotificationData.EXTRA_ID, -1)
        val extraVal1 = inputData.getInt(PostNotificationData.EXTRA_VAL_1, -1)
        val extraVal2 = inputData.getInt(PostNotificationData.EXTRA_VAL_2, -1)

        val notifyIntent = Intent(applicationContext, targetClass).apply {
            putExtra(
                INTENT_NOTIFICATION_EXTRAS,
                Json.encodeToString(
                    NotificationIntentExtras(
                        mediaId = mediaId,
                        mediaType = MediaType.valueOf(mediaType!!),
                        extraId = if (extraId != -1) extraId else null,
                        extraValue1 = if (extraVal1 != -1) extraVal1 else null,
                        extraValue2 = if (extraVal2 != -1) extraVal2 else null,
                    ),
                ),
            )

            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            applicationContext,
            "$mediaType-$mediaId".hashCode(),
            notifyIntent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
        )
    }
}
