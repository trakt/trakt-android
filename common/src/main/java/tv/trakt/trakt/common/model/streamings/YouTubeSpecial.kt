package tv.trakt.trakt.common.model.streamings

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.networking.api.v3.model.V3YouTubeSpecialResponse

/**
 * A free YouTube upload of a stand-up special, published by the comedian or their label
 * (official) or by someone else (fan upload). The API omits the source for official uploads.
 */
@Immutable
data class YouTubeSpecial(
    val videoId: String,
    val isOfficial: Boolean,
) {
    val link: String
        get() = "https://www.youtube.com/watch?v=$videoId"

    companion object {
        const val SOURCE = "youtube"

        fun isSupported(genres: List<MediaGenre>): Boolean {
            return MediaGenre.Comedy in genres
        }

        fun fromDto(dto: V3YouTubeSpecialResponse): YouTubeSpecial {
            return YouTubeSpecial(
                videoId = dto.videoId,
                isOfficial = dto.source == null || dto.source == OFFICIAL_SOURCE,
            )
        }
    }
}

private const val OFFICIAL_SOURCE = "official"
