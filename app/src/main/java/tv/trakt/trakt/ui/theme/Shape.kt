@file:Suppress("ktlint:standard:property-naming")

package tv.trakt.trakt.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

internal val DefaultCardShape = RoundedCornerShape(24.dp)

private val SegmentInnerCorner = CornerSize(4.dp)

internal fun segmentShape(
    index: Int,
    count: Int,
): Shape {
    val first = index == 0
    val last = index == count - 1

    return when {
        first && last -> DefaultCardShape
        first -> DefaultCardShape.copy(bottomStart = SegmentInnerCorner, bottomEnd = SegmentInnerCorner)
        last -> DefaultCardShape.copy(topStart = SegmentInnerCorner, topEnd = SegmentInnerCorner)
        else -> RoundedCornerShape(SegmentInnerCorner)
    }
}
