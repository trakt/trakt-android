package tv.trakt.trakt.core.notifications.push.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.applinks.model.AppLink
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType.CommentMention
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType.CommentReaction
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType.CommentReply
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType.ListComment
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType.ListReaction
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType.NewFollower
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType.NewFollowerRequest
import tv.trakt.trakt.core.notifications.push.model.PushNotificationType.PendingCollaboration

internal const val PUSH_EXTRA_TYPE = "type"
internal const val PUSH_EXTRA_PAYLOAD = "payload"

private val payloadJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class PushPayload(
    @SerialName("comment_id")
    val commentId: Int? = null,
    @SerialName("list_id")
    val listId: Int? = null,
    @SerialName("item_id")
    val itemId: Int? = null,
    @SerialName("follower_id")
    val followerId: Int? = null,
)

/**
 * Maps a server push (`data.type` and the JSON `data.payload`) to the screen a tap opens.
 * Returns null for unknown types or a malformed payload, which only opens the app.
 */
internal fun parsePushNotificationLink(
    type: String?,
    payload: String?,
): AppLink? {
    val pushType = PushNotificationType.fromValue(type) ?: return null
    if (payload.isNullOrBlank()) return null

    val data = runCatching { payloadJson.decodeFromString<PushPayload>(payload) }
        .getOrNull()
        ?: return null

    return when (pushType) {
        CommentMention,
        CommentReply,
        CommentReaction,
        ListComment,
        -> data.commentId?.let { AppLink.Comment(it.toTraktId()) }
        ListReaction -> data.listId?.let { AppLink.UserList(it.toTraktId()) }
        PendingCollaboration -> data.itemId?.let { AppLink.UserList(it.toTraktId()) }
        NewFollower,
        NewFollowerRequest,
        -> data.followerId?.let { AppLink.User(it.toTraktId()) }
    }
}
