package tv.trakt.trakt.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("ktlint:standard:property-naming")
internal const val VerticalImageAspectRatio = 2f / 3

internal val Size: TraktSize = TraktSize(
    navigationBarHeight = 80.dp,
    verticalMediaCardSize = 128.dp,
    horizontalMediaCardSize = 160.dp,
)

@Immutable
internal data class TraktSize(
    val navigationBarHeight: Dp = Dp.Unspecified,
    val horizontalMediaCardSize: Dp = Dp.Unspecified,
    val verticalMediaCardSize: Dp = Dp.Unspecified,
)
