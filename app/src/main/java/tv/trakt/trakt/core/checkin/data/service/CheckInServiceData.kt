package tv.trakt.trakt.core.checkin.data.service

import android.os.Bundle
import tv.trakt.trakt.common.model.MediaType
import java.time.Instant

internal data class CheckInServiceData(
    val mediaId: Int,
    val mediaType: MediaType,
    val mediaImage: String?,
    val title: String,
    val startedAt: Instant,
    val expiresAt: Instant,
) {
    companion object Key {
        const val TITLE = "title"
        const val MEDIA_ID = "mediaId"
        const val MEDIA_TYPE = "mediaType"
        const val MEDIA_IMAGE = "mediaImage"
        const val STARTED_AT = "startedAt"
        const val EXPIRES_AT = "expiresAt"

        fun fromBundle(bundle: Bundle): CheckInServiceData {
            return CheckInServiceData(
                title = bundle.getString(TITLE, "")!!,
                mediaId = bundle.getInt(MEDIA_ID, -1),
                mediaType = MediaType.valueOf(bundle.getString(MEDIA_TYPE)!!),
                mediaImage = bundle.getString(MEDIA_IMAGE, ""),
                startedAt = Instant.ofEpochMilli(bundle.getLong(STARTED_AT)),
                expiresAt = Instant.ofEpochMilli(bundle.getLong(EXPIRES_AT)),
            )
        }
    }

    fun toBundle(): Bundle {
        return Bundle().apply {
            putString(TITLE, title)
            putInt(MEDIA_ID, mediaId)
            putString(MEDIA_TYPE, mediaType.name)
            putString(MEDIA_IMAGE, mediaImage)
            putLong(STARTED_AT, startedAt.toEpochMilli())
            putLong(EXPIRES_AT, expiresAt.toEpochMilli())
        }
    }
}
