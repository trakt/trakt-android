package tv.trakt.trakt.helpers

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import tv.trakt.trakt.ui.theme.TraktTheme

/**
 * State for a screen header that can collapse when scrolling.
 */
internal class ScreenHeaderState(
    maxHeaderHeightPx: Int,
) {
    var startScrolled by mutableStateOf(false)
        private set
    var scrolled by mutableStateOf(false)
        private set

    val connection = CollapsingBarConnection(
        barMaxHeight = maxHeaderHeightPx.toFloat(),
        onStartScrollUp = { startScrolled = true },
        onScrollUp = { scrolled = true },
    )

    fun resetScrolled() {
        connection.resetScrolled()
        scrolled = false
    }

    fun resetOffset() {
        connection.resetOffset()
    }

    companion object {
        val Saver = run {
            val keyBarOffset = "barOffset"
            val keyBarMaxHeight = "barMaxHeight"
            val keyScrolled = "scrolled"
            val keyStartScrolled = "startScrolled"
            mapSaver(
                save = {
                    mapOf(
                        keyBarOffset to it.connection.barOffset,
                        keyBarMaxHeight to it.connection.barMaxHeight.toInt(),
                        keyScrolled to it.scrolled,
                        keyStartScrolled to it.startScrolled,
                    )
                },
                restore = {
                    ScreenHeaderState(
                        it[keyBarMaxHeight] as Int,
                    ).apply {
                        scrolled = it[keyScrolled] as Boolean
                        startScrolled = it[keyStartScrolled] as Boolean
                        connection.barOffset = it[keyBarOffset] as Float
                    }
                },
            )
        }
    }
}

@Composable
internal fun rememberHeaderState(): ScreenHeaderState {
    val density = LocalDensity.current
    val headerHeight = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()
        .plus(TraktTheme.size.navigationHeaderHeight)

    return rememberSaveable(
        headerHeight,
        saver = ScreenHeaderState.Saver,
    ) {
        ScreenHeaderState(
            maxHeaderHeightPx = with(density) {
                headerHeight.roundToPx()
            },
        )
    }
}
