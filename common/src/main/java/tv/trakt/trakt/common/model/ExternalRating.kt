package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.resources.R
import java.util.Locale

@Immutable
data class ExternalRating(
    val imdb: ImdbRating?,
    val meta: MetaRating?,
    val rotten: RottenRating?,
) {
    @Immutable
    data class ImdbRating(
        val rating: Float,
        val votes: Int,
        val link: String?,
    ) {
        val ratingString: String
            get() = String.format(Locale.ROOT, "%.0f%%", rating * 10)
    }

    @Immutable
    data class MetaRating(
        val rating: Int,
        val link: String?,
    )

    @Immutable
    data class RottenRating(
        val rating: Float,
        val state: String?,
        val userRating: Int?,
        val userState: String?,
        val link: String?,
    ) {
        val ratingIcon: Int
            get() = when (state) {
                "certified" -> R.drawable.ic_rotten_certified
                "fresh" -> R.drawable.ic_rotten_tomato
                "rotten" -> R.drawable.ic_rotten_splash
                else -> R.drawable.ic_rotten_tomato
            }

        val userRatingIcon: Int
            get() = when (userState) {
                "certified" -> R.drawable.ic_rotten_audience_certified
                "upright" -> R.drawable.ic_rotten_audience_upright
                "spilled" -> R.drawable.ic_rotten_audience_spilled
                else -> R.drawable.ic_rotten_audience_upright
            }
    }
}
