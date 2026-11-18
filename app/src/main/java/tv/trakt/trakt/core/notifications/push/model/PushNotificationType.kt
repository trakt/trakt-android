package tv.trakt.trakt.core.notifications.push.model

import tv.trakt.trakt.core.notifications.TraktNotificationChannel
import tv.trakt.trakt.core.notifications.TraktNotificationChannel.SOCIAL_COLLABORATIONS
import tv.trakt.trakt.core.notifications.TraktNotificationChannel.SOCIAL_COMMENTS
import tv.trakt.trakt.core.notifications.TraktNotificationChannel.SOCIAL_FOLLOWERS
import tv.trakt.trakt.core.notifications.TraktNotificationChannel.SOCIAL_REACTIONS

/**
 * Server push types (`data.type`). The server sends [channel] as `android.notification.channel_id`
 * so background pushes land in the same channel as foreground ones.
 */
internal enum class PushNotificationType(
    val value: String,
    val channel: TraktNotificationChannel,
) {
    NewFollower("new_follower_payload", SOCIAL_FOLLOWERS),
    NewFollowerRequest("new_follower_request_payload", SOCIAL_FOLLOWERS),
    CommentMention("comment_mention_payload", SOCIAL_COMMENTS),
    CommentReply("comment_reply_payload", SOCIAL_COMMENTS),
    CommentReaction("comment_like_payload", SOCIAL_REACTIONS),
    ListComment("list_comment_payload", SOCIAL_COMMENTS),
    ListReaction("list_like_payload", SOCIAL_REACTIONS),
    PendingCollaboration("pending_collaboration_payload", SOCIAL_COLLABORATIONS),
    ;

    companion object {
        fun fromValue(value: String?): PushNotificationType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
