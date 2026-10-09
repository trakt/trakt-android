package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.UserMediaDto

@Serializable
data class V3RecommendedByResponse(
    val users: List<UserMediaDto>,
    @SerialName("other_count")
    val otherCount: Int,
)
