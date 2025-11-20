package tv.trakt.trakt.ui.extensions

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

internal fun WindowSizeClass.isAtLeastMedium(): Boolean {
    return isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
}
