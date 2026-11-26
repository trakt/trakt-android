package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MixtapeVectorsDto(
    val cutoff: Double,
    val vectors: Map<String, String> = emptyMap(),
)
