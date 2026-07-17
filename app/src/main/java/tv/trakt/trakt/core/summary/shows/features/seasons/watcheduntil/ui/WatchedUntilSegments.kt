package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

internal val SegmentHeight = 46.dp
internal val SegmentGap = 4.dp

private val SegmentCornerLarge = 16.dp
private val SegmentCornerSmall = 6.dp

internal enum class SegmentPosition {
    Top,
    Middle,
    Bottom,
}

internal fun SegmentPosition.shape(): Shape =
    when (this) {
        SegmentPosition.Top -> RoundedCornerShape(
            topStart = SegmentCornerLarge,
            topEnd = SegmentCornerLarge,
            bottomStart = SegmentCornerSmall,
            bottomEnd = SegmentCornerSmall,
        )

        SegmentPosition.Middle -> RoundedCornerShape(SegmentCornerSmall)

        SegmentPosition.Bottom -> RoundedCornerShape(
            topStart = SegmentCornerSmall,
            topEnd = SegmentCornerSmall,
            bottomStart = SegmentCornerLarge,
            bottomEnd = SegmentCornerLarge,
        )
    }

internal fun leadingSegmentShape(): Shape =
    RoundedCornerShape(
        topStart = SegmentCornerLarge,
        bottomStart = SegmentCornerLarge,
        topEnd = SegmentCornerSmall,
        bottomEnd = SegmentCornerSmall,
    )

internal fun trailingSegmentShape(): Shape =
    RoundedCornerShape(
        topStart = SegmentCornerSmall,
        bottomStart = SegmentCornerSmall,
        topEnd = SegmentCornerLarge,
        bottomEnd = SegmentCornerLarge,
    )
