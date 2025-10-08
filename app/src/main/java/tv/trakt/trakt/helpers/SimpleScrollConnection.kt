package tv.trakt.trakt.helpers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

internal class SimpleScrollConnection : NestedScrollConnection {
    var resultOffset: Float by mutableFloatStateOf(0F)
        private set

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val delta = consumed.y
        resultOffset = resultOffset + delta

        return Offset.Companion.Zero
    }

    companion object {
        val Saver = Saver<SimpleScrollConnection, Float>(
            save = { it.resultOffset },
            restore = {
                SimpleScrollConnection().apply {
                    resultOffset = it
                }
            }
        )
    }
}
