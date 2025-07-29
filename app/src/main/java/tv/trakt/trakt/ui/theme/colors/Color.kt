package tv.trakt.trakt.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade300
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.common.ui.theme.colors.Shade940
import tv.trakt.trakt.common.ui.theme.colors.White

internal val DarkColors: TraktColors = TraktColors(
    accent = Purple500,
    backgroundPrimary = Shade940,
    textPrimary = White,
    textSecondary = Shade300,
    skeletonContainer = Shade800,
    skeletonShimmer = Shade700,
    chipContainer = Shade800,
    chipContent = Color.White,
    navigationContainer = Shade920.copy(alpha = 0.98F),
    navigationContent = White,
)

@Immutable
internal data class TraktColors(
    val accent: Color = Color.Unspecified,
    val backgroundPrimary: Color = Color.Unspecified,
    val textPrimary: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val skeletonContainer: Color = Color.Unspecified,
    val skeletonShimmer: Color = Color.Unspecified,
    val chipContainer: Color = Color.Unspecified,
    val chipContent: Color = Color.Unspecified,
    // Nav
    val navigationContainer: Color = Color.Unspecified,
    val navigationContent: Color = Color.Unspecified,
)
