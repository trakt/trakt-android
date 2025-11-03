package tv.trakt.trakt.common.model.ratings

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

/**
 * User rating for a media item.
 */
@Serializable
data class UserRating(
    val mediaId: TraktId,
    val mediaType: MediaType,
    val rating: Int,
) {
    /**
     * Rating scaled to 1-5 range.
     */
    val ratingsScaled: Int = when {
        rating >= 9 -> 5
        rating >= 7 -> 4
        rating >= 5 -> 3
        rating >= 3 -> 2
        else -> 1
    }
}
