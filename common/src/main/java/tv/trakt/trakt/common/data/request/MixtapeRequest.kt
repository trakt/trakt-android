package tv.trakt.trakt.common.data.request

import kotlinx.serialization.Serializable

@Serializable
data class MixtapeRequest(
    val type: String,
    val picks: List<Int>,
    val candidates: List<Int>,
    val suggestions: Boolean,
)
