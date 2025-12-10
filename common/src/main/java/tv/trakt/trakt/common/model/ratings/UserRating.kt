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
    val favorite: Boolean = false,
) {
    companion object {
        fun scaleTo10(rating: Float): Int {
            require(rating in 0.5F..5F) { "Rating must be between 0.5 and 5" }
            return (rating * 2).toInt()
        }
    }
}
