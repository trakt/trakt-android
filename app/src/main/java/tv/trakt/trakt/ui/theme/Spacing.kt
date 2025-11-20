package tv.trakt.trakt.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val Spacing: TraktSpacing = TraktSpacing(
    mainRowSpace = 10.dp,
    mainRowHeaderSpace = 14.dp,
    mainListVerticalSpace = 14.dp,
    mainGridHorizontalSpace = 10.dp,
    mainGridVerticalSpace = 14.dp,
    mainPageHorizontalSpace = 16.dp,
    mainPageTopSpace = 74.dp,
    mainPageBottomSpace = 48.dp,
    mainSectionVerticalSpace = 32.dp,
    chipsSpace = 4.dp,
    filterChipsSpace = 6.dp,
    contextItemsSpace = 12.dp,
    detailsHeaderHorizontalSpace = 64.dp,
    detailsActionsHorizontalSpace = 42.dp,
)

internal val MediumSpacing: TraktSpacing = Spacing.copy(
    mainPageTopSpace = 90.dp,
    mainPageHorizontalSpace = 24.dp,
    detailsHeaderHorizontalSpace = 192.dp,
    detailsActionsHorizontalSpace = 192.dp,
)

@Immutable
internal data class TraktSpacing(
    val mainListVerticalSpace: Dp = Dp.Unspecified,
    val mainGridHorizontalSpace: Dp = Dp.Unspecified,
    val mainGridVerticalSpace: Dp = Dp.Unspecified,
    val mainRowSpace: Dp = Dp.Unspecified,
    val mainRowHeaderSpace: Dp = Dp.Unspecified,
    val mainPageHorizontalSpace: Dp = Dp.Unspecified,
    val mainPageTopSpace: Dp = Dp.Unspecified,
    val mainPageBottomSpace: Dp = Dp.Unspecified,
    val mainSectionVerticalSpace: Dp = Dp.Unspecified,
    val chipsSpace: Dp = Dp.Unspecified,
    val filterChipsSpace: Dp = Dp.Unspecified,
    val contextItemsSpace: Dp = Dp.Unspecified,
    val detailsHeaderHorizontalSpace: Dp = Dp.Unspecified,
    val detailsActionsHorizontalSpace: Dp = Dp.Unspecified,
)
