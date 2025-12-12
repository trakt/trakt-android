package tv.trakt.trakt.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.common.ui.theme.colors.Purple100
import tv.trakt.trakt.common.ui.theme.colors.Purple300
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Purple800
import tv.trakt.trakt.common.ui.theme.colors.Purple900
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade300
import tv.trakt.trakt.common.ui.theme.colors.Shade600
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.common.ui.theme.colors.Shade900
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.common.ui.theme.colors.Shade940
import tv.trakt.trakt.common.ui.theme.colors.White

internal val DarkColors: TraktColors = TraktColors(
    accent = Purple500,
    backgroundPrimary = Shade940,
    textPrimary = White,
    textSecondary = Shade300,
    skeletonContainer = Shade920,
    skeletonShimmer = Shade900,
    placeholderContainer = Shade800,
    placeholderContent = Shade600,
    chipContainer = Shade800,
    chipContainerOnContent = Shade800.copy(alpha = 0.64F),
    chipContent = Color.White,
    navigationHeaderContainer = Shade920.copy(alpha = 0.98F),
    navigationContainer = Shade920.copy(alpha = 0.98F),
    navigationContent = White,
    inputContainer = Shade940,
    dialogContainer = Shade920,
    dialogContent = Shade800,
    panelCardContainer = Shade920,
    commentContainer = Shade920,
    commentReplyContainer = Shade900,
    customListContainer = Shade920,
    sentimentsContainer = Purple900,
    sentimentsAccent = Purple100,
    detailsStatus1 = Purple300,
    detailsStatus2 = Purple100,
    vipAccent = Red500,
    // Buttons
    primaryButtonContainer = Purple500,
    primaryButtonContainerDisabled = Shade700,
    primaryButtonContent = Color.White,
    primaryButtonContentDisabled = Color.White,
    // Snackbar
    snackbarContainer = White,
    snackbarContent = Shade940,
    // Switches
    switchContainerChecked = Purple800,
    switchContainerUnchecked = White,
    switchThumbChecked = Purple500,
    switchThumbUnchecked = Purple500,
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
    val dialogContainer: Color = Color.Unspecified,
    val dialogContent: Color = Color.Unspecified,
    val panelCardContainer: Color = Color.Unspecified,
    val commentContainer: Color = Color.Unspecified,
    val commentReplyContainer: Color = Color.Unspecified,
    val customListContainer: Color = Color.Unspecified,
    val sentimentsContainer: Color = Color.Unspecified,
    val sentimentsAccent: Color = Color.Unspecified,
    val detailsStatus1: Color = Color.Unspecified,
    val detailsStatus2: Color = Color.Unspecified,
    val vipAccent: Color = Color.Unspecified,
    // Nav
    val navigationHeaderContainer: Color = Color.Unspecified,
    val navigationContainer: Color = Color.Unspecified,
    val navigationContent: Color = Color.Unspecified,
    // Buttons
    val primaryButtonContainer: Color = Color.Unspecified,
    val primaryButtonContainerDisabled: Color = Color.Unspecified,
    val primaryButtonContent: Color = Color.Unspecified,
    val primaryButtonContentDisabled: Color = Color.Unspecified,
    // Snackbar
    val snackbarContainer: Color = Color.Unspecified,
    val snackbarContent: Color = Color.Unspecified,
    // Switches
    val switchContainerChecked: Color = Color.Unspecified,
    val switchContainerUnchecked: Color = Color.Unspecified,
    val switchThumbChecked: Color = Color.Unspecified,
    val switchThumbUnchecked: Color = Color.Unspecified,
)
