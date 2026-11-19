package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LivingPosterIndexDto(
    val v: Int? = null,
    val movies: List<Int>? = null,
    val shows: List<Int>? = null,
)

@Serializable
data class LivingPosterManifestDto(
    val v: Int? = null,
    val layers: List<JsonElement>? = null,
    val mask: String? = null,
    val effects: List<JsonElement>? = null,
    val cut: String? = null,
)
