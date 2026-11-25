package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MediaMatchDto(
    val match: Int? = null,
    val taste: List<Taste>? = null,
) {
    @Serializable
    data class Taste(
        val slug: String,
        val name: String,
        val label: String,
    )
}
