package tv.trakt.trakt.common.networking.api.klipy.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KlipyCategoriesDto(
    val categories: List<KlipyCategoryDto> = emptyList(),
)

@Serializable
data class KlipyCategoryDto(
    val category: String,
    val query: String,
    @SerialName("preview_url")
    val previewUrl: String,
)
