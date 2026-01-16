package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Immutable
@Serializable
data class Rating(
    val rating: Float,
    val votes: Int,
) {
    val ratingPercent: Int
        get() = (rating * 10).roundToInt()

    val rating5Scale: String
        get() = "%.1f".format(rating / 2f)
}
