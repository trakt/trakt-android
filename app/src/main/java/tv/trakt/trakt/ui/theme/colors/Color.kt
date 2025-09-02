package tv.trakt.trakt.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade300
import tv.trakt.trakt.common.ui.theme.colors.Shade600
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
    placeholderContainer = Shade800,
    placeholderContent = Shade600,
    chipContainer = Shade800,
    chipContainerOnContent = Shade800.copy(alpha = 0.66F),
    chipContent = Color.White,
    navigationHeaderContainer = Shade920.copy(alpha = 0.98F),
    navigationContainer = Shade920.copy(alpha = 0.98F),
    navigationContent = White,
    inputContainer = Shade940,
    // Buttons
    primaryButtonContainer = Purple500,
    primaryButtonContainerDisabled = Shade700,
    primaryButtonContent = Color.White,
    primaryButtonContentDisabled = Color.White,
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
    val chipContainerOnContent: Color = Color.Unspecified,
    val chipContent: Color = Color.Unspecified,
    val placeholderContainer: Color = Color.Unspecified,
    val placeholderContent: Color = Color.Unspecified,
    val inputContainer: Color = Color.Unspecified,
    // Nav
    val navigationHeaderContainer: Color = Color.Unspecified,
    val navigationContainer: Color = Color.Unspecified,
    val navigationContent: Color = Color.Unspecified,
    // Buttons
    val primaryButtonContainer: Color = Color.Unspecified,
    val primaryButtonContainerDisabled: Color = Color.Unspecified,
    val primaryButtonContent: Color = Color.Unspecified,
    val primaryButtonContentDisabled: Color = Color.Unspecified,
)
