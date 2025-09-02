package tv.trakt.trakt.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val Spacing: TraktSpacing = TraktSpacing(
    mainRowSpace = 10.dp,
    mainGridHorizontalSpace = 10.dp,
    mainGridVerticalSpace = 14.dp,
    mainRowHeaderSpace = 14.dp,
    mainPageHorizontalSpace = 16.dp,
    mainPageTopSpace = 88.dp,
    mainPageBottomSpace = 64.dp,
    mainSectionVerticalSpace = 30.dp,
)

@Immutable
internal data class TraktSpacing(
    val mainGridHorizontalSpace: Dp = Dp.Unspecified,
    val mainGridVerticalSpace: Dp = Dp.Unspecified,
    val mainRowSpace: Dp = Dp.Unspecified,
    val mainRowHeaderSpace: Dp = Dp.Unspecified,
    val mainPageHorizontalSpace: Dp = Dp.Unspecified,
    val mainPageTopSpace: Dp = Dp.Unspecified,
    val mainPageBottomSpace: Dp = Dp.Unspecified,
    val mainSectionVerticalSpace: Dp = Dp.Unspecified,
)
