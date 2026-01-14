package tv.trakt.trakt.core.notifications.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.MediaType

@Serializable
internal data class NotificationIntentExtras(
    val mediaId: Int,
    val mediaType: MediaType,
    val extraId: Int? = null,
    val extraValue1: Int? = null,
    val extraValue2: Int? = null,
)
