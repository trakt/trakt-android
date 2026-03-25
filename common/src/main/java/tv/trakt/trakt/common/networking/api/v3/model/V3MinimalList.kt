package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3MinimalList(
    val id: Int,
    val name: String,
    val count: Int,
    val type: String,
    @SerialName("display_order")
    val displayOrder: Int,
    @SerialName("owner_id")
    val ownerId: Int,
)
