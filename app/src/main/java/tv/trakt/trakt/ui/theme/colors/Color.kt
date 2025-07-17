package tv.trakt.trakt.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade300
import tv.trakt.trakt.common.ui.theme.colors.Shade940
import tv.trakt.trakt.common.ui.theme.colors.White

internal val DarkColors: TraktColors = TraktColors(
    accent = Purple500,
    backgroundPrimary = Shade940,
    textPrimary = White,
    textSecondary = Shade300,
)

@Immutable
internal data class TraktColors(
    val accent: Color = Color.Unspecified,
    val backgroundPrimary: Color = Color.Unspecified,
    val textPrimary: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
)
