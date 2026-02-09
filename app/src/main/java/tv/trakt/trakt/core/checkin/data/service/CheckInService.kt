package tv.trakt.trakt.core.checkin.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Build
import android.os.IBinder
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.notifications.TraktNotificationChannel
import tv.trakt.trakt.core.notifications.data.work.INTENT_NOTIFICATION_EXTRAS
import tv.trakt.trakt.core.notifications.model.NotificationIntentExtras
import tv.trakt.trakt.resources.R
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.seconds

private const val SERVICE_ID = 997
private const val EXTRA_DATA = "data"
private const val MAIN_ACTIVITY_PATH = "tv.trakt.trakt.MainActivity"

internal class CheckInService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val bundleData = requireNotNull(intent?.getBundleExtra(EXTRA_DATA)) {
            "Check-in service started without data!"
        }

        val serviceData = CheckInServiceData.fromBundle(bundleData)
        val notification = setNotification(data = serviceData)

        @SuppressLint("InlinedApi")
        ServiceCompat.startForeground(
            this@CheckInService,
            SERVICE_ID,
            notification,
            FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )

        scope.launch {
            while (isActive) {
                delay(5.seconds)
                updateProgress(serviceData)
            }
        }

        Timber.d("Check-in service started.")
        return START_NOT_STICKY
    }

    private fun setNotification(data: CheckInServiceData): Notification {
        val now = nowUtcInstant().epochSecond
        val startedAt = data.startedAt.epochSecond
        val expiresAt = data.expiresAt.epochSecond

        val progress = ((now - startedAt).toFloat() / (expiresAt - startedAt).toFloat()) * 100f
        val minutesLeft = ((expiresAt - now) / 60F).roundToLong().coerceAtLeast(1)
        val secondsLeft = (expiresAt - now).coerceAtLeast(0)

        // Ensure progress is at least 1 to show the progress bar
        val normalizedProgress = progress.toInt().coerceIn(1, 100)
        val contentText = when {
            secondsLeft > 60 -> getString(R.string.text_time_remaining, minutesLeft.durationFormat())
            else -> getString(R.string.text_time_remaining, "<${1L.durationFormat()}")
        }

        val notification = NotificationCompat
            .Builder(applicationContext, TraktNotificationChannel.CHECK_IN.id)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setSmallIcon(R.drawable.ic_trakt_icon_notification)
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
            .setContentIntent(createNotificationIntent(data))

        return notification.build()
    }

    private fun updateProgress(serviceData: CheckInServiceData) {
        if (!checkPermission()) {
            // Missing notification permission, stop the service
            Timber.w("Missing POST_NOTIFICATIONS permission for check-in service")
            stopSelf()
            return
        }

        if (serviceData.expiresAt <= nowUtcInstant()) {
            // Check-in expired, stop the service
            Timber.d("Check-in expired, stopping service")
            stopSelf()
            return
        }

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                SERVICE_ID,
                setNotification(data = serviceData),
            )
    }

    private fun checkPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationIntent(data: CheckInServiceData): PendingIntent {
        val targetClass = Class.forName(MAIN_ACTIVITY_PATH)

        val notifyIntent = Intent(applicationContext, targetClass).apply {
            putExtra(
                INTENT_NOTIFICATION_EXTRAS,
                Json.encodeToString(
                    NotificationIntentExtras(
                        mediaId = data.mediaId,
                        mediaType = data.mediaType,
                        extraId = data.extraId,
                        extraValue1 = data.extraValue1,
                        extraValue2 = data.extraValue2,
                    ),
                ),
            )

            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            applicationContext,
            "${data.mediaType}-${data.mediaId}".hashCode(),
            notifyIntent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(
            context: Context,
            data: CheckInServiceData,
        ) {
            val intent = Intent(context, CheckInService::class.java).apply {
                putExtra(EXTRA_DATA, data.toBundle())
            }
            context.stopService(intent)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CheckInService::class.java)
            context.stopService(intent)
        }
    }
}
