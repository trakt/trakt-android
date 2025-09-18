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
    navigationHeaderHeight = 64.dp,
    verticalMediaCardSize = 130.dp,
    verticalMediumMediaCardSize = 80.dp,
    verticalSmallMediaCardSize = 72.dp,
    horizontalMediaCardSize = 200.dp,
    horizontalSmallMediaCardSize = 128.dp,
)

@Immutable
internal data class TraktSize(
    val titleBarHeight: Dp = Dp.Unspecified,
    val navigationBarHeight: Dp = Dp.Unspecified,
    val navigationHeaderHeight: Dp = Dp.Unspecified,
    val horizontalMediaCardSize: Dp = Dp.Unspecified,
    val horizontalSmallMediaCardSize: Dp = Dp.Unspecified,
    val verticalMediaCardSize: Dp = Dp.Unspecified,
    val verticalMediumMediaCardSize: Dp = Dp.Unspecified,
    val verticalSmallMediaCardSize: Dp = Dp.Unspecified,
)
