package tv.trakt.trakt.core.notifications.model

import androidx.work.Data
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.core.notifications.TraktNotificationChannel
import java.time.Instant

internal data class PostNotificationData(
    val channel: TraktNotificationChannel,
    val mediaId: Int,
    val mediaType: MediaType,
    val mediaImage: String?,
    val title: String,
    val content: String,
    val targetDate: Instant,
) {
    companion object Key {
        const val CHANNEL = "channel"
        const val TITLE = "title"
        const val CONTENT = "content"
        const val MEDIA_ID = "mediaId"
        const val MEDIA_TYPE = "mediaType"
        const val MEDIA_IMAGE = "mediaImage"
    }

    fun toInputData(): Data {
        return Data.Builder()
            .putString(CHANNEL, channel.id)
            .putString(TITLE, title)
            .putString(CONTENT, content)
            .putInt(MEDIA_ID, mediaId)
            .putString(MEDIA_TYPE, mediaType.value)
            .putString(MEDIA_IMAGE, mediaImage)
            .build()
    }
}
