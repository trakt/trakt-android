package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageManifestDto(
    val poster: ImageManifestSlotDto? = null,
)

@Serializable
data class ImageManifestSlotDto(
    val langs: List<String>? = null,
)
