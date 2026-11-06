package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class V3SoundtrackTrackResponse(
    val title: String,
    val performer: String? = null,
    @SerialName("spotify_id") val spotifyId: String? = null,
    @SerialName("matched_on") val matchedOn: String? = null,
    val position: Int,
    val season: Int? = null,
    val source: String? = null,
)
