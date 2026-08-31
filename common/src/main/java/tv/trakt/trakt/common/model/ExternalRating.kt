package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.networking.TraktRatingsDto
import tv.trakt.trakt.resources.R
import java.util.Locale
import kotlin.math.roundToInt

@Immutable
data class ExternalRating(
    val trakt: TraktRating?,
    val imdb: ImdbRating?,
    val tmdb: TmdbRating?,
    val meta: MetaRating?,
    val rotten: RottenRating?,
    val mal: MalRating?,
    val letterboxd: LetterboxdRating?,
) {
    @Immutable
    data class TraktRating(
        val rating: Float,
        val votes: Int,
        val distribution: ImmutableMap<Int, Float>,
    ) {
        val ratingPercent: Int
            get() = (rating * 10).roundToInt()

        /**
         * Collapses the 1-10 vote distribution into 5star buckets (1-2 -> 1, ..., 9-10 -> 5).
         */
        val starDistribution: ImmutableMap<Int, Float>
            get() = (1..5)
                .associateWith { star ->
                    (distribution[star * 2 - 1] ?: 0F) + (distribution[star * 2] ?: 0F)
                }
                .toImmutableMap()

        companion object {
            fun fromDto(dto: TraktRatingsDto): TraktRating =
                TraktRating(
                    rating = dto.rating,
                    votes = dto.votes,
                    distribution = mapOf(
                        1 to dto.distribution._1,
                        2 to dto.distribution._2,
                        3 to dto.distribution._3,
                        4 to dto.distribution._4,
                        5 to dto.distribution._5,
                        6 to dto.distribution._6,
                        7 to dto.distribution._7,
                        8 to dto.distribution._8,
                        9 to dto.distribution._9,
                        10 to dto.distribution._10,
                    ).toImmutableMap(),
                )
        }
    }

    @Immutable
    data class ImdbRating(
        val rating: Float,
        val votes: Int,
        val link: String?,
    ) {
        val ratingString: String
            get() = String.format(Locale.ROOT, "%.1f", rating)
    }

    @Immutable
    data class TmdbRating(
        val rating: Float,
        val votes: Int,
        val link: String?,
    ) {
        val ratingString: String
            get() = String.format(Locale.ROOT, "%.1f", rating)
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

    @Immutable
    data class MalRating(
        val rating: Float,
        val votes: Int,
        val link: String?,
    ) {
        val ratingString: String
            get() = String.format(Locale.ROOT, "%.1f", rating)
    }

    data class LetterboxdRating(
        val rating: Float,
        val votes: Int,
        val link: String?,
    ) {
        val ratingString: String
            get() = String.format(Locale.ROOT, "%.1f", rating)
    }
}
