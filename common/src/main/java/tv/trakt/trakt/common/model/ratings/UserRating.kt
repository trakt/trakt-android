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
    companion object {
        fun scaleTo10(rating: Int): Int {
            require(rating in 1..5) { "Rating must be between 1 and 5" }
            return when (rating) {
                5 -> 10
                4 -> 8
                3 -> 6
                2 -> 4
                1 -> 2
                else -> throw IllegalArgumentException("Rating must be between 1 and 5")
            }
        }
    }
}
