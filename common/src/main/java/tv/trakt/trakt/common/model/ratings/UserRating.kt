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
    val key: String
        get() = "${mediaType.value}-${mediaId.value}"

    val rating5Scale: String
        get() = "%.1f"
            .format(rating / 2f)
            .removeSuffix(".0")

    companion object {
        fun scaleTo10(rating: Float): Int {
            require(rating in 0.5F..5F) { "Rating must be between 0.5 and 5" }
            return (rating * 2).toInt()
        }
    }
}
