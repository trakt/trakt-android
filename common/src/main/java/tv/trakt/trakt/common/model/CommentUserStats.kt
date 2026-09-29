package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
data class CommentUserStats(
    val playCount: Int,
    val completedCount: Int,
    val rating: Int?,
) {
    val completedPercent: String
        get() {
            if (playCount <= 0) return "0%"
            val percent = (completedCount * 100F / playCount)
                .roundToInt()
                .coerceIn(0, 100)
            return "$percent%"
        }
}
