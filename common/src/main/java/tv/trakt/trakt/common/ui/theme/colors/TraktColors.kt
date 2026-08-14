package tv.trakt.trakt.common.ui.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TraktColors(
    val isLight: Boolean = false,
    val accent: Color = Color.Unspecified,
    val backgroundPrimary: Color = Color.Unspecified,
    val backgroundImageAlpha: Float = Float.NaN,
    val textPrimary: Color = Color.Unspecified,
    val textPrimaryOnAccent: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val skeletonContainer: Color = Color.Unspecified,
    val skeletonShimmer: Color = Color.Unspecified,
    val tagChipContainer: Color = Color.Unspecified,
    val tagChipContainerLight: Color = Color.Unspecified,
    val tagChipContent: Color = Color.Unspecified,
    val tagChipContentOnAccent: Color = Color.Unspecified,
    val chipContainer: Color = Color.Unspecified,
    val chipContainerOnContent: Color = Color.Unspecified,
    val chipContent: Color = Color.Unspecified,
    val placeholderContainer: Color = Color.Unspecified,
    val placeholderContent: Color = Color.Unspecified,
    val inputContainer: Color = Color.Unspecified,
    val dialogContainer: Color = Color.Unspecified,
    val dialogOnContainer: Color = Color.Unspecified,
    val dialogContent: Color = Color.Unspecified,
    // Dropdowns
    val dropdownContainer: Color = Color.Unspecified,
    val dropdownContainerActive: Color = Color.Unspecified,
    val dropdownMenuContainer: Color = Color.Unspecified,
    val dropdownContent: Color = Color.Unspecified,
    val dropdownContentActive: Color = Color.Unspecified,
    val panelCardContainer: Color = Color.Unspecified,
    val commentContainer: Color = Color.Unspecified,
    val commentReplyContainer: Color = Color.Unspecified,
    val customListContainer: Color = Color.Unspecified,
    val customListGradient: Color = Color.Unspecified,
    val sentimentsContainer: Color = Color.Unspecified,
    val sentimentsAccent: Color = Color.Unspecified,
    val sentimentsGoodAccent: Color = Color.Unspecified,
    val sentimentsBadAccent: Color = Color.Unspecified,
    val triviaContainer: Color = Color.Unspecified,
    val triviaAccent: Color = Color.Unspecified,
    val detailsStatus1: Color = Color.Unspecified,
    val detailsStatus2: Color = Color.Unspecified,
    val vipAccent: Color = Color.Unspecified,
    val separator: Color = Color.Unspecified,
    // Streaks
    val streakTileEmpty: Color = Color.Unspecified,
    val streakTileToday: Color = Color.Unspecified,
    val streakLevel1: Color = Color.Unspecified,
    val streakLevel2: Color = Color.Unspecified,
    val streakLevel3: Color = Color.Unspecified,
    val streakLevel4: Color = Color.Unspecified,
    // Nav
    val navigationHeaderContainer: Color = Color.Unspecified,
    val navigationContainer: Color = Color.Unspecified,
    val navigationContent: Color = Color.Unspecified,
    val navigationContentOn: Color = Color.Unspecified,
    val navigationContentOff: Color = Color.Unspecified,
    // Buttons
    val primaryButtonContainer: Color = Color.Unspecified,
    val primaryButtonContainerDisabled: Color = Color.Unspecified,
    val primaryButtonContent: Color = Color.Unspecified,
    val primaryButtonContentDisabled: Color = Color.Unspecified,
    val ghostButtonContent: Color = Color.Unspecified,
    // Snackbar
    val snackbarContainer: Color = Color.Unspecified,
    val snackbarContent: Color = Color.Unspecified,
    // Tooltip
    val tooltipContainer: Color = Color.Unspecified,
    val tooltipContent: Color = Color.Unspecified,
    // Switches
    val switchContainerChecked: Color = Color.Unspecified,
    val switchContainerUnchecked: Color = Color.Unspecified,
    val switchThumbChecked: Color = Color.Unspecified,
    val switchThumbUnchecked: Color = Color.Unspecified,
    // Reactions
    val reactionsContainer: Color = Color.Unspecified,
    val reactionsSummaryContainer: Color = Color.Unspecified,
    val reactionsSummaryHighlight: Color = Color.Unspecified,
    // Shadows
    val shadowDefault: Dp = 0.dp,
    val shadowSmall: Dp = 0.dp,
    val shadowLarge: Dp = 0.dp,
    val shadowDynamicDefault: Dp = 0.dp,
    val shadowDynamicSmall: Dp = 0.dp,
    val shadowDynamicLarge: Dp = 0.dp,
    // Misc
    val profileStatsContainer: Color = Color.Unspecified,
    val profileStatsChip: Color = Color.Unspecified,
)
