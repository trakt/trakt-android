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
    val extraId: Int? = null,
    val extraValue1: Int? = null,
    val extraValue2: Int? = null,
) {
    companion object Key {
        const val CHANNEL = "channel"
        const val TITLE = "title"
        const val CONTENT = "content"
        const val MEDIA_ID = "mediaId"
        const val MEDIA_TYPE = "mediaType"
        const val MEDIA_IMAGE = "mediaImage"
        const val EXTRA_ID = "extraId"
        const val EXTRA_VAL_1 = "extraVal1"
        const val EXTRA_VAL_2 = "extraVal2"
    }

    fun toInputData(): Data {
        return Data.Builder()
            .putString(CHANNEL, channel.id)
            .putString(TITLE, title)
            .putString(CONTENT, content)
            .putInt(MEDIA_ID, mediaId)
            .putString(MEDIA_TYPE, mediaType.name)
            .putString(MEDIA_IMAGE, mediaImage)
            .putInt(EXTRA_ID, extraId ?: -1)
            .putInt(EXTRA_VAL_1, extraValue1 ?: -1)
            .putInt(EXTRA_VAL_2, extraValue2 ?: -1)
            .build()
    }
}
