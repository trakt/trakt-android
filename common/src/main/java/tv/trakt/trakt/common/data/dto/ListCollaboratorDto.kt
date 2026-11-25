package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListCollaboratorDto(
    val username: String,
    val ids: Ids,
    val name: String? = null,
    @SerialName("private")
    val isPrivate: Boolean = false,
    val vip: Boolean? = null,
    @SerialName("vip_ep")
    val vipEp: Boolean? = null,
    @SerialName("vip_og")
    val vipOg: Boolean? = null,
    val director: Boolean? = null,
    val location: String? = null,
    val about: String? = null,
) {
    @Serializable
    data class Ids(
        val trakt: Int? = null,
        val slug: String? = null,
    )
}
