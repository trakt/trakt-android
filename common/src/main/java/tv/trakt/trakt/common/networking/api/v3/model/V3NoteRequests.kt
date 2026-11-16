package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.Serializable

@Serializable
data class V3PostNoteRequest(
    val media: Media,
    val type: String,
    val notes: String,
) {
    @Serializable
    data class Media(
        val type: String,
        val id: Int,
    )
}

@Serializable
data class V3EditNoteRequest(
    val type: String,
    val notes: String,
)

@Serializable
data class V3DeleteNoteRequest(
    val type: String,
)
