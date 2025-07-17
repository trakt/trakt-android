package tv.trakt.trakt.tv.common.model

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
internal data class Rating(
    val rating: Float,
    val votes: Int,
) {
    val ratingPercent: Int
        get() = (rating * 10).roundToInt()
}
