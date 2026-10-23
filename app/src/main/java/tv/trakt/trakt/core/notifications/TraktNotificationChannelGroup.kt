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
    CHECK_IN(
        id = "id_channel_group_checkin",
        title = "Now Watching (Check In)",
    ),
    RECAP(
        id = "id_channel_group_recap",
        title = "Recap",
    ),
    ;

    fun createChannelGroup(): NotificationChannelGroup {
        return NotificationChannelGroup(
            id,
            title,
        )
    }
}
