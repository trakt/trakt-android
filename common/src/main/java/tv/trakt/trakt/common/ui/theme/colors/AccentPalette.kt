package tv.trakt.trakt.common.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AccentPalette(
    val shade50: Color,
    val shade100: Color,
    val shade200: Color,
    val shade300: Color,
    val shade400: Color,
    val shade600: Color,
    val shade900: Color,
    val shade1000: Color,
)

val PurplePalette = AccentPalette(
    shade50 = Purple50,
    shade100 = Purple100,
    shade200 = Purple200,
    shade300 = Purple300,
    shade400 = Purple400,
    shade600 = Purple600,
    shade900 = Purple900,
    shade1000 = Purple1000,
)

val OrangePalette = AccentPalette(
    shade50 = Orange50,
    shade100 = Orange100,
    shade200 = Orange200,
    shade300 = Orange300,
    shade400 = Orange400,
    shade600 = Orange600,
    shade900 = Orange900,
    shade1000 = Orange1000,
)

val AmberPalette = AccentPalette(
    shade50 = Amber50,
    shade100 = Amber100,
    shade200 = Amber200,
    shade300 = Amber300,
    shade400 = Amber400,
    shade600 = Amber600,
    shade900 = Amber900,
    shade1000 = Amber1000,
)

val EmeraldPalette = AccentPalette(
    shade50 = Emerald50,
    shade100 = Emerald100,
    shade200 = Emerald200,
    shade300 = Emerald300,
    shade400 = Emerald400,
    shade600 = Emerald600,
    shade900 = Emerald900,
    shade1000 = Emerald1000,
)

val RedPalette = AccentPalette(
    shade50 = Red50,
    shade100 = Red100,
    shade200 = Red200,
    shade300 = Red300,
    shade400 = Red400,
    shade600 = Red600,
    shade900 = Red900,
    shade1000 = Red1000,
)
