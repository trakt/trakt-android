package tv.trakt.trakt.common.core.streamings.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId

/**
 * Identifies the media a watchnow lookup is for.
 *
 * [countryCode] `null` asks for every country the media streams in; a code narrows the
 * response down to that single country.
 */
@Immutable
data class StreamingsRequest(
    val mediaType: MediaType,
    val mediaId: TraktId,
    val seasonEpisode: SeasonEpisode? = null,
    val countryCode: String? = null,
) {
    init {
        require(mediaType != MediaType.Season) {
            "Unsupported media type: $mediaType"
        }
        if (mediaType == MediaType.Episode) {
            requireNotNull(seasonEpisode) {
                "seasonEpisode is required for $mediaType"
            }
        }
    }
}
