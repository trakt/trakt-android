package tv.trakt.trakt.core.settings.features.younify.data.remote.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class YounifyDetailsDto(
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
internal data class YounifyAvailableServicesDto(
    val watched: List<YounifyServiceDto>,
    val ratings: List<YounifyServiceDto>,
)

@Serializable
internal data class YounifyLinkedServicesDto(
    val active: List<YounifyServiceDto>,
    val inactive: List<YounifyServiceDto>,
)

@Serializable
internal data class YounifyServiceDto(
    val id: String,
    val name: String,
    val color: String,
    val images: YounifyServiceImagesDto,
)

@Serializable
internal data class YounifyServiceImagesDto(
    val logo: String,
)
