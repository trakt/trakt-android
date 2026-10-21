package tv.trakt.trakt.common.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Shade300

private val DefaultIndicatorSize = 40.dp

@Composable
fun FilmProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Shade300,
    size: Dp = 48.dp,
) {
    CircularProgressIndicator(
        color = color,
        strokeWidth = 5.dp * (size / DefaultIndicatorSize),
        modifier = modifier.size(size),
    )
}
