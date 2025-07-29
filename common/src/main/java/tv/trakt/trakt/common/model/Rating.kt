package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
data class Rating(
    val rating: Float,
    val votes: Int,
) {
    val ratingPercent: Int
        get() = (rating * 10).roundToInt()
}
