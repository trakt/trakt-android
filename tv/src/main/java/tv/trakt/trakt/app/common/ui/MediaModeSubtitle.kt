package tv.trakt.trakt.app.common.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.MediaMode

@Composable
internal fun MediaModeSubtitle(
    mode: MediaMode,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(mode.offIcon),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer {
                    translationY = -0.5.dp.toPx()
                },
        )
        Text(
            text = stringResource(mode.displayRes),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(8.dp),
        ) {
            MediaMode.entries.forEach {
                MediaModeSubtitle(mode = it)
            }
        }
    }
}
