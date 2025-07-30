package tv.trakt.trakt.helpers

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
}

@Composable
internal fun rememberHeaderState(): ScreenHeaderState {
    val density = LocalDensity.current

    val headerHeight = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()
        .plus(TraktTheme.size.navigationHeaderHeight)

    return remember {
        ScreenHeaderState(
            maxHeaderHeightPx = with(density) {
                headerHeight.roundToPx()
            },
        )
    }
}
