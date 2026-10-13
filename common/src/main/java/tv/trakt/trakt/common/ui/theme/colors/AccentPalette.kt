package tv.trakt.trakt.common.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AccentPalette(
    val shade50: Color,
    val shade200: Color,
    val shade300: Color,
    val shade400: Color,
    val shade600: Color,
    val shade900: Color,
    val shade1000: Color,
)

val PurplePalette = AccentPalette(
    shade50 = Purple50,
    shade200 = Purple200,
    shade300 = Purple300,
    shade400 = Purple400,
    shade600 = Purple600,
    shade900 = Purple900,
    shade1000 = Purple1000,
)

val OrangePalette = AccentPalette(
    shade50 = Orange50,
    shade200 = Orange200,
    shade300 = Orange300,
    shade400 = Orange400,
    shade600 = Orange600,
    shade900 = Orange900,
    shade1000 = Orange1000,
)
