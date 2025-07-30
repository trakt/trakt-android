package tv.trakt.trakt.helpers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * A [NestedScrollConnection] that handles the collapsing behavior of a top bar.
 */
internal class CollapsingBarConnection(
    val barMaxHeight: Float,
    val onStartScrollUp: () -> Unit = {},
    val onScrollUp: () -> Unit = {},
) : NestedScrollConnection {
    var barOffset: Float by mutableFloatStateOf(0F)
        private set

    private var startScrolledUp: Boolean = false
    private var scrolledUp: Boolean = false

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val delta = available.y
        val newOffset = barOffset + delta
        barOffset = newOffset.coerceIn(-barMaxHeight, 0F)

        if (!startScrolledUp && newOffset <= -10F) {
            onStartScrollUp()
            startScrolledUp = true
        }

        if (!scrolledUp && newOffset <= -barMaxHeight) {
            onScrollUp()
            scrolledUp = true
        }

        return Offset.Companion.Zero
    }
}
