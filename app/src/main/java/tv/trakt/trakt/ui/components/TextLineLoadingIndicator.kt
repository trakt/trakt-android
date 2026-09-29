package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TextLineLoadingIndicator(
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier,
    ) {
        // An empty Text measures one line at the current font scale and reserves its height.
        Text(
            text = "",
            style = style,
            maxLines = 1,
        )
        FilmProgressIndicator(
            size = size,
            color = color,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        TextLineLoadingIndicator(
            style = TraktTheme.typography.paragraphSmaller,
            color = TraktTheme.colors.textSecondary,
        )
    }
}
