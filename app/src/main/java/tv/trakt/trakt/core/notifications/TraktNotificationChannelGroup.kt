package tv.trakt.trakt.core.notifications

import android.app.NotificationChannelGroup

enum class TraktNotificationChannelGroup(
    val id: String,
    val title: String,
) {
    MEDIA(
        id = "id_channel_group_media",
        title = "Media",
    ),
    ;

    fun createChannelGroup(): NotificationChannelGroup {
        return NotificationChannelGroup(
            id,
            title,
        )
    }
}
