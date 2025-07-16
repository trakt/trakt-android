package tv.trakt.app.tv.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White

val DarkColors: TraktColors = TraktColors(
    accent = Purple500,
    backgroundPrimary = Shade940,
    textPrimary = White,
    textSecondary = Shade300,
    progressPrimary = Shade300,
    chipContainer = Shade800,
    chipContent = White,
    placeholderContainer = Shade800,
    placeholderContent = Shade600,
    skeletonContainer = Shade800,
    skeletonShimmer = Shade700,
    commentContainer = Shade920,
    commentReplyContainer = Shade900,
    customListContainer = Shade920,
    customOfficialListContainer = Purple900,
    // Navigation
    navigationBackground = Color(0xF52E3337),
    navigationItemOn = Purple400,
    navigationItemOff = Shade300,
    // Buttons
    primaryButtonContainer = Purple500,
    primaryButtonContainerDisabled = Shade700,
    primaryButtonContent = White,
    primaryButtonContentDisabled = White,
    // Dialogs
    dialogContainer = Shade900,
    // Snackbars
    snackbarContainer = White,
    snackbarContent = Black,
)

@Immutable
data class TraktColors(
    val accent: Color = Color.Unspecified,
    val backgroundPrimary: Color = Color.Unspecified,
    val textPrimary: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val progressPrimary: Color = Color.Unspecified,
    val chipContainer: Color = Color.Unspecified,
    val chipContent: Color = Color.Unspecified,
    val placeholderContainer: Color = Color.Unspecified,
    val placeholderContent: Color = Color.Unspecified,
    val skeletonContainer: Color = Color.Unspecified,
    val skeletonShimmer: Color = Color.Unspecified,
    val commentContainer: Color = Color.Unspecified,
    val commentReplyContainer: Color = Color.Unspecified,
    val customListContainer: Color = Color.Unspecified,
    val customOfficialListContainer: Color = Color.Unspecified,
    // Buttons
    val primaryButtonContainer: Color = Color.Unspecified,
    val primaryButtonContainerDisabled: Color = Color.Unspecified,
    val primaryButtonContent: Color = Color.Unspecified,
    val primaryButtonContentDisabled: Color = Color.Unspecified,
    // Navigation
    val navigationBackground: Color = Color.Unspecified,
    val navigationItemOn: Color = Color.Unspecified,
    val navigationItemOff: Color = Color.Unspecified,
    // Dialogs
    val dialogContainer: Color = Color.Unspecified,
    // Snackbars
    val snackbarContainer: Color = Color.Unspecified,
    val snackbarContent: Color = Color.Unspecified,
)
