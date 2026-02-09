package tv.trakt.trakt.core.checkin.data.service

import android.os.Bundle
import tv.trakt.trakt.common.model.MediaType
import java.time.Instant

internal data class CheckInServiceData(
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val startedAt: Instant,
    val expiresAt: Instant,
    val extraId: Int? = null,
    val extraValue1: Int? = null,
    val extraValue2: Int? = null,
) {
    companion object Key {
        const val TITLE = "title"
        const val MEDIA_ID = "mediaId"
        const val MEDIA_TYPE = "mediaType"
        const val STARTED_AT = "startedAt"
        const val EXPIRES_AT = "expiresAt"
        const val EXTRA_ID = "extraId"
        const val EXTRA_VALUE_1 = "extraValue1"
        const val EXTRA_VALUE_2 = "extraValue2"

        fun fromBundle(bundle: Bundle): CheckInServiceData {
            return CheckInServiceData(
                title = bundle.getString(TITLE, "")!!,
                mediaId = bundle.getInt(MEDIA_ID, -1),
                mediaType = MediaType.valueOf(bundle.getString(MEDIA_TYPE)!!),
                startedAt = Instant.ofEpochMilli(bundle.getLong(STARTED_AT)),
                expiresAt = Instant.ofEpochMilli(bundle.getLong(EXPIRES_AT)),
                extraId = bundle.getInt(EXTRA_ID).takeIf { bundle.containsKey(EXTRA_ID) },
                extraValue1 = bundle.getInt(EXTRA_VALUE_1).takeIf { bundle.containsKey(EXTRA_VALUE_1) },
                extraValue2 = bundle.getInt(EXTRA_VALUE_2).takeIf { bundle.containsKey(EXTRA_VALUE_2) },
            )
        }
    }

    fun toBundle(): Bundle {
        return Bundle().apply {
            putString(TITLE, title)
            putInt(MEDIA_ID, mediaId)
            putString(MEDIA_TYPE, mediaType.name)
            putLong(STARTED_AT, startedAt.toEpochMilli())
            putLong(EXPIRES_AT, expiresAt.toEpochMilli())
            extraId?.let { putInt(EXTRA_ID, it) }
            extraValue1?.let { putInt(EXTRA_VALUE_1, it) }
            extraValue2?.let { putInt(EXTRA_VALUE_2, it) }
        }
    }
}
