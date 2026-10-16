package tv.trakt.trakt.common.core.younify.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YounifyRefreshRequest(
    @SerialName("service_id")
    val serviceID: String,
    @SerialName("skip_sync")
    val skipSync: Boolean,
)
