package tv.trakt.trakt.tv.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Spacing: TraktSpacing = TraktSpacing(
    mainRowSpace = 10.dp,
    mainGridSpace = 10.dp,
    mainRowHeaderSpace = 12.dp,
    mainRowVerticalSpace = 28.dp,
    mainContentStartSpace = 94.dp,
    mainContentEndSpace = 48.dp,
    mainContentVerticalSpace = 24.dp,
)

@Immutable
data class TraktSpacing(
    val mainRowSpace: Dp = Dp.Unspecified,
    val mainGridSpace: Dp = Dp.Unspecified,
    val mainRowHeaderSpace: Dp = Dp.Unspecified,
    val mainRowVerticalSpace: Dp = Dp.Unspecified,
    val mainContentStartSpace: Dp = Dp.Unspecified,
    val mainContentEndSpace: Dp = Dp.Unspecified,
    val mainContentVerticalSpace: Dp = Dp.Unspecified,
)
