package tv.trakt.trakt.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager

enum class TraktNotificationChannel(
    val id: String,
    val title: String,
    val description: String,
    val importance: Int,
) {
    SHOWS(
        id = "id_channel_shows",
        title = "Shows releases",
        description = "New shows, seasons and episodes releases",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    MOVIES(
        id = "id_channel_movies",
        title = "Movies releases",
        description = "New movies releases",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    ;

    fun createChannel(): NotificationChannel {
        return NotificationChannel(
            id,
            title,
            importance,
        ).apply {
            description = this@TraktNotificationChannel.description
        }
    }
}
