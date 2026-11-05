package tv.trakt.trakt.common.model.soundtrack

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.api.v3.model.V3SoundtrackTrackResponse

@Immutable
data class SoundtrackTrack(
    val title: String,
    val performer: String?,
    val spotifyId: String?,
    val matchedOn: String?,
    val position: Int,
    val season: Int?,
) {
    val isPlayable: Boolean
        get() = !spotifyId.isNullOrBlank()

    companion object {
        fun fromDto(dto: V3SoundtrackTrackResponse): SoundtrackTrack {
            return SoundtrackTrack(
                title = dto.title,
                performer = dto.performer?.trim()?.ifBlank { null },
                spotifyId = dto.spotifyId,
                matchedOn = dto.matchedOn,
                position = dto.position,
                season = dto.season,
            )
        }
    }
}
