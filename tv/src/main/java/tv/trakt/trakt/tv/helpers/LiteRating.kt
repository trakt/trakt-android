package tv.trakt.trakt.tv.helpers

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.ui.theme.colors.Red500

enum class LiteRating(
    @param:DrawableRes val iconRes: Int,
    val tint: Color,
) {
    LIKE(R.drawable.ic_thumb_up_fill, Color.White),
    DISLIKE(R.drawable.ic_thumb_down_fill, Color.White),
    LOVE(R.drawable.ic_heart, Red500),
    ;

    companion object {
        fun fromValue(value: Int): LiteRating {
            return when (value.coerceIn(0..10)) {
                in 0..5 -> DISLIKE
                in 6..8 -> LIKE
                in 9..10 -> LOVE
                else -> throw IllegalArgumentException("Invalid rating value: $value")
            }
        }
    }
}
