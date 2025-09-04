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

    fun resetScrolled() {
        scrolledUp = false
    }

    fun resetOffset() {
        barOffset = 0F
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val delta = consumed.y

        val newBarOffset = barOffset + delta
        barOffset = newBarOffset.coerceIn(-barMaxHeight, 0F)

        if (!startScrolledUp && newBarOffset <= -10F) {
            onStartScrollUp()
            startScrolledUp = true
        }

        if (!scrolledUp && newBarOffset <= -barMaxHeight) {
            onScrollUp()
            scrolledUp = true
        }

        return Offset.Companion.Zero
    }
}
