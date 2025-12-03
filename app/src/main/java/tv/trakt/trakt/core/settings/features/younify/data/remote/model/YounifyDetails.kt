package tv.trakt.trakt.core.settings.features.younify.data.remote.model

import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.core.settings.features.younify.data.remote.model.dto.YounifyDetailsDto

@Serializable
internal data class YounifyDetails(
    val id: String,
    val tokens: YounifyTokens,
    val services: YounifyServices,
) {
    companion object {
        fun fromDto(dto: YounifyDetailsDto): YounifyDetails {
            return YounifyDetails(
                id = dto.id,
                tokens = YounifyTokens(
                    accessToken = dto.accessToken,
                    refreshToken = dto.refreshToken,
                ),
                services = YounifyServices(
                    available = YounifyServices.Available(
                        watched = dto.availableServices.watched
                            .map {
                                YounifyService(
                                    id = it.id,
                                    name = it.name,
                                    color = it.color,
                                    images = YounifyService.Images(
                                        logo = it.images.logo,
                                    ),
                                )
                            }
                            .toImmutableList(),
                    ),
//                    linked = YounifyServices.Linked(
//                        active = EmptyImmutableList,
//                        inactive = EmptyImmutableList,
//                    ),
                ),
            )
        }
    }
}
