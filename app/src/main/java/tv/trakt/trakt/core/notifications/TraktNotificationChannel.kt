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
    CHECK_IN(
        id = "id_channel_checkin",
        title = "Now Watching (Check In)",
        description = "Notifications related to your current watching activity",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    MONTH_REVIEW(
        id = "id_channel_month_review",
        title = "Month in Review",
        description = "Monthly watching recap notifications",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    SOCIAL_COMMENTS(
        id = "id_channel_social_comments",
        title = "Comments",
        description = "Replies, mentions and comments on your lists",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    SOCIAL_REACTIONS(
        id = "id_channel_social_reactions",
        title = "Reactions",
        description = "Reactions to your comments and lists",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    SOCIAL_FOLLOWERS(
        id = "id_channel_social_followers",
        title = "Followers",
        description = "New followers and follow requests",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    SOCIAL_COLLABORATIONS(
        id = "id_channel_social_collaborations",
        title = "List invites",
        description = "Invites to collaborate on lists",
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
