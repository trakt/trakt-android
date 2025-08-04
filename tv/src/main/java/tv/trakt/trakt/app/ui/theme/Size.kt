package tv.trakt.trakt.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val Size: TraktSize = TraktSize(
    verticalMediaCardSize = 102.dp,
    horizontalMediaCardSize = 160.dp,
    detailsPosterSize = 335.dp,
    detailsPersonPosterSize = 102.dp,
    detailsCommentSize = 176.dp,
    detailsCustomListSize = 184.dp,
    navigationDrawerSize = 184.dp,
)

@Immutable
internal data class TraktSize(
    val horizontalMediaCardSize: Dp = Dp.Unspecified,
    val verticalMediaCardSize: Dp = Dp.Unspecified,
    val detailsPosterSize: Dp = Dp.Unspecified,
    val detailsPersonPosterSize: Dp = Dp.Unspecified,
    val detailsCommentSize: Dp = Dp.Unspecified,
    val detailsCustomListSize: Dp = Dp.Unspecified,
    val navigationDrawerSize: Dp = Dp.Unspecified,
)
