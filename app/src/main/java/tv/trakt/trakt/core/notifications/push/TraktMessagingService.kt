package tv.trakt.trakt.core.notifications.push

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.notifications.data.work.MAIN_ACTIVITY_PATH
import tv.trakt.trakt.core.notifications.push.data.work.SyncPushTokenWorker
import tv.trakt.trakt.core.notifications.push.model.PUSH_EXTRA_PAYLOAD
import tv.trakt.trakt.core.notifications.push.model.PUSH_EXTRA_TYPE
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType
import tv.trakt.trakt.resources.R

// FCM posts background notifications with this id, so a foreground repeat with the same tag replaces it.
private const val PUSH_NOTIFICATION_ID = 0

internal class TraktMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        SyncPushTokenWorker.schedule(applicationContext)
    }

    /**
     * Called for notification messages only while the app is in the foreground.
     * In the background FCM shows them itself and passes `data` to the launch intent.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("Push message received: $message")

        val content = message.notification ?: return
        val channel = PushNotificationType.fromValue(message.data[PUSH_EXTRA_TYPE])?.channel ?: return
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, POST_NOTIFICATIONS) != PERMISSION_GRANTED
        ) {
            return
        }

        val tag = content.tag ?: message.messageId
        val notification = Notification
            .Builder(applicationContext, channel.id)
            .setContentIntent(createContentIntent(message, tag))
            .setSmallIcon(R.drawable.ic_trakt_icon_notification)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(Notification.BigTextStyle().bigText(content.body))
            .setAutoCancel(true)
            .setColor(Purple500.toArgb())
            .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(tag, PUSH_NOTIFICATION_ID, notification)
    }

    private fun createContentIntent(
        message: RemoteMessage,
        tag: String?,
    ): PendingIntent {
        val intent = Intent(applicationContext, Class.forName(MAIN_ACTIVITY_PATH)).apply {
            putExtra(PUSH_EXTRA_TYPE, message.data[PUSH_EXTRA_TYPE])
            putExtra(PUSH_EXTRA_PAYLOAD, message.data[PUSH_EXTRA_PAYLOAD])
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return PendingIntent.getActivity(
            applicationContext,
            tag.hashCode(),
            intent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
        )
    }
}
