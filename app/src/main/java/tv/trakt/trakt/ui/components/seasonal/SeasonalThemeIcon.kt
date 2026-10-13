package tv.trakt.trakt.ui.components.seasonal

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

@Composable
internal fun SeasonalThemeIcon(
    modifier: Modifier = Modifier,
    season: SeasonalTheme,
    enabled: Boolean,
    size: Dp = 26.dp,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = when {
            enabled -> TraktTheme.colors.accentPalette.shade400
            else -> TraktTheme.colors.textPrimary
        },
    )

    Icon(
        painter = painterResource(season.icon),
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(size)
            .onClick(onClick = onClick),
    )
}

@Preview
@Composable
private fun SeasonalThemeIconPreview() {
    TraktTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            SeasonalThemeIcon(
                season = SeasonalTheme.Halloween,
                enabled = true,
                onClick = {},
            )
            SeasonalThemeIcon(
                season = SeasonalTheme.Halloween,
                enabled = false,
                onClick = {},
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}
