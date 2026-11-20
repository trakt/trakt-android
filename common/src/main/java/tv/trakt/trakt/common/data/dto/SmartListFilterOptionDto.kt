package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SmartListFilterOptionDto(
    val name: String,
    val slug: String? = null,
)
