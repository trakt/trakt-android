package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class V3SyncConnectionResponse(
    val id: Long,
    val provider: String,
    @SerialName("server_name")
    val serverName: String?,
    @SerialName("collection_limit_reached")
    val collectionLimitReached: Boolean,
)
