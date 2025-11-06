@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package tv.trakt.trakt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults.toggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val options = listOf(
    R.string.button_text_toggle_search_media,
    R.string.button_text_shows,
    R.string.button_text_movies,
)

val offIcons = listOf(
    R.drawable.ic_shows_movies,
    R.drawable.ic_shows_off,
    R.drawable.ic_movies_off,
)

val onIcons = listOf(
    R.drawable.ic_shows_movies_on,
    R.drawable.ic_shows_on,
    R.drawable.ic_movies_on,
)

@Composable
internal fun MediaModeButtons(
    modifier: Modifier = Modifier,
    height: Dp = 35.dp,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier,
    ) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = {
                    if (selectedIndex != index) {
                        selectedIndex = index
                    }
                },
                colors = toggleButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = TraktTheme.colors.textPrimary,
                    checkedContainerColor = TraktTheme.colors.accent,
                    checkedContentColor = TraktTheme.colors.textPrimary,
                ),
                border = when {
                    selectedIndex == index -> BorderStroke(
                        width = 0.dp,
                        color = Color.Transparent,
                    )
                    else -> BorderStroke(
                        width = 1.dp,
                        color = TraktTheme.colors.chipContainer,
                    )
                },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                ),
                modifier = Modifier.height(height),
            ) {
                Icon(
                    painter = painterResource(
                        when (selectedIndex == index) {
                            true -> onIcons[index]
                            false -> offIcons[index]
                        },
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )

                Spacer(Modifier.size(5.dp))

                Text(
                    text = stringResource(label),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.buttonTertiary,
                )
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        MediaModeButtons()
    }
}
