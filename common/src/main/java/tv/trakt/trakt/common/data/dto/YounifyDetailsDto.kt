package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YounifyDetailsDto(
    val id: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("available_services")
    val availableServices: YounifyAvailableServicesDto,
//    @SerialName("linked_services")
//    val linkedServices: YounifyLinkedServicesDto,
)

@Serializable
data class YounifyAvailableServicesDto(
    val watched: List<YounifyServiceDto>,
    val ratings: List<YounifyServiceDto>,
)

@Serializable
data class YounifyLinkedServicesDto(
    val active: List<YounifyServiceDto>,
    val inactive: List<YounifyServiceDto>,
)

@Serializable
data class YounifyServiceDto(
    val id: String,
    val name: String,
    val color: String,
    val vip: Boolean,
    val images: YounifyServiceImagesDto,
)

@Serializable
data class YounifyServiceImagesDto(
    val logo: String,
)
