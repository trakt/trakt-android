package tv.trakt.trakt.core.settings.features.younify.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class YounifyRefreshRequest(
    @SerialName("service_id")
    val serviceID: String,
    @SerialName("skip_sync")
    val skipSync: Boolean,
)
