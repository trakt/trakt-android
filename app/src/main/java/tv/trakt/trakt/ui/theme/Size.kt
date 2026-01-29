@file:Suppress("ktlint:standard:property-naming")

package tv.trakt.trakt.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val VerticalImageAspectRatio = 2f / 3
internal const val HorizontalImageAspectRatio = 16f / 9

internal val Size: TraktSize = TraktSize(
    titleBarHeight = 56.dp,
    navigationBarHeight = 80.dp,
    navigationBarRatio = 1F,
    navigationHeaderHeight = 64.dp,
    verticalMediaCardSize = 130.dp,
    verticalMediumMediaCardSize = 84.dp,
    verticalSmallMediaCardSize = 72.dp,
    horizontalMediaCardSize = 200.dp,
    horizontalSmallMediaCardSize = 128.dp,
    mainGridColumns = 3,
    calendarGridColumns = 2,
    commentCardSize = 192.dp,
    customListCardSize = 192.dp,
    detailsBackgroundRatio = VerticalImageAspectRatio,
)

internal val MediumSize: TraktSize = Size.copy(
    navigationBarRatio = 0.66F,
    navigationHeaderHeight = 80.dp,
    detailsBackgroundRatio = VerticalImageAspectRatio * 1.6F,
    mainGridColumns = 4,
    calendarGridColumns = 4,
)

internal val LargeSize: TraktSize = Size.copy(
    navigationBarRatio = 0.4F,
    navigationHeaderHeight = 80.dp,
    detailsBackgroundRatio = VerticalImageAspectRatio * 1.5F,
    mainGridColumns = 8,
    calendarGridColumns = 6,
)

@Immutable
internal data class TraktSize(
    val titleBarHeight: Dp = Dp.Unspecified,
    val navigationBarHeight: Dp = Dp.Unspecified,
    val navigationBarRatio: Float = 1F,
    val navigationHeaderHeight: Dp = Dp.Unspecified,
    val horizontalMediaCardSize: Dp = Dp.Unspecified,
    val horizontalSmallMediaCardSize: Dp = Dp.Unspecified,
    val verticalMediaCardSize: Dp = Dp.Unspecified,
    val verticalMediumMediaCardSize: Dp = Dp.Unspecified,
    val verticalSmallMediaCardSize: Dp = Dp.Unspecified,
    val mainGridColumns: Int = 3,
    val calendarGridColumns: Int = 2,
    val commentCardSize: Dp = Dp.Unspecified,
    val customListCardSize: Dp = Dp.Unspecified,
    val detailsBackgroundRatio: Float = 1F,
)
