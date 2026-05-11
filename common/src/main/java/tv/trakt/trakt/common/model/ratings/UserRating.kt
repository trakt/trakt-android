package tv.trakt.trakt.common.model.ratings

import android.content.res.Resources
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R
import java.util.Locale

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
            .format(Locale.US, rating / 2f)
            .removeSuffix(".0")
            .removeSuffix(",0")

    companion object {
        fun scaleTo10(rating: Float): Int {
            require(rating in 0.5F..5F) { "Rating must be between 0.5 and 5" }
            return (rating * 2).toInt()
        }

        fun getSlug(
            rating: Float,
            resources: Resources,
        ): String {
            return when (rating) {
                0.5F -> resources.getString(R.string.text_rating_1)
                1F -> resources.getString(R.string.text_rating_2)
                1.5F -> resources.getString(R.string.text_rating_3)
                2F -> resources.getString(R.string.text_rating_4)
                2.5F -> resources.getString(R.string.text_rating_5)
                3F -> resources.getString(R.string.text_rating_6)
                3.5F -> resources.getString(R.string.text_rating_7)
                4F -> resources.getString(R.string.text_rating_8)
                4.5F -> resources.getString(R.string.text_rating_9)
                5F -> resources.getString(R.string.text_rating_10)
                else -> ""
            }
        }
    }
}
