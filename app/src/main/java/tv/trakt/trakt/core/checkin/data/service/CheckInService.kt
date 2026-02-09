package tv.trakt.trakt.core.checkin.data.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.IBinder
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.notifications.TraktNotificationChannel
import tv.trakt.trakt.resources.R
import kotlin.math.roundToLong

private const val SERVICE_ID = 997
private const val EXTRA_DATA = "data"

internal class CheckInService : Service() {
    companion object {
        fun start(
            context: Context,
            data: CheckInServiceData,
        ) {
            val intent = Intent(context, CheckInService::class.java).apply {
                putExtra(EXTRA_DATA, data.toBundle())
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CheckInService::class.java)
            context.stopService(intent)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    @SuppressLint("InlinedApi")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val bundleData = requireNotNull(intent?.getBundleExtra(EXTRA_DATA)) {
            "Check-in service started without data!"
        }

        val serviceData = CheckInServiceData.fromBundle(bundleData)

        val now = nowUtcInstant().epochSecond
        val startedAt = serviceData.startedAt.epochSecond
        val expiresAt = serviceData.expiresAt.epochSecond

        val progress = ((now - startedAt).toFloat() / (expiresAt - startedAt).toFloat()) * 100f
        val minutesLeft = ((expiresAt - now) / 60F).roundToLong().coerceAtLeast(1)
        val secondsLeft = (expiresAt - now).coerceAtLeast(0)

        val notification = createNotification(
            data = serviceData,
            progress = progress.toInt().coerceIn(0, 100),
            contentText = when {
                secondsLeft > 60 -> getString(R.string.text_time_remaining, minutesLeft.durationFormat())
                else -> getString(R.string.text_time_remaining, "<${1L.durationFormat()}")
            },
        )

        ServiceCompat.startForeground(
            this@CheckInService,
            SERVICE_ID,
            notification,
            FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )

        Timber.d("Check-in service started.")
        return START_NOT_STICKY
    }

    private fun createNotification(
        data: CheckInServiceData,
        progress: Int,
        contentText: String,
    ): Notification {
        // Ensure progress is at least 1 to show the progress bar
        val normalizedProgress = progress.coerceAtLeast(1)

        val notification = NotificationCompat
            .Builder(applicationContext, TraktNotificationChannel.CHECK_IN.id)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setSmallIcon(R.drawable.ic_trakt_icon)
            .setSubText(getString(R.string.text_now_watching))
            .setContentTitle(data.title)
            .setContentText(contentText)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setProgress(100, normalizedProgress, false)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(Purple500.toArgb())
            .setColorized(true)

//        if (!data.mediaImage.isNullOrBlank()) {
//            try {
//                val imageLoader = ImageLoader(applicationContext)
//                val request = ImageRequest.Builder(applicationContext)
//                    .data(data.mediaImage)
//                    .allowHardware(false) // Disable hardware bitmaps for notifications
//                    .build()
//
//                val result = imageLoader.execute(request)
//                val bitmap = (result.image as? BitmapDrawable)?.bitmap
//                    ?: result.image?.toBitmap()
//
//                if (bitmap != null) {
//                    notification.setLargeIcon(bitmap)
//                }
//            } catch (error: Exception) {
//                Timber.e(error, "Failed to load notification image")
//            }
//        }

        return notification.build()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
