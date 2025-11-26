package tv.trakt.trakt.ui.extensions

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

private const val WIDTH_DP_LARGE_LOWER_BOUND: Int = 720

internal fun WindowSizeClass.isAtLeastMedium(): Boolean {
    return isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
}

internal fun WindowSizeClass.isAtLeastLarge(): Boolean {
    return isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND)
}
