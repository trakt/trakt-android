@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.all.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsMode
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SeasonsModeButtons(
    mode: SeasonsMode,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 36.dp,
    onModeSelect: (SeasonsMode) -> Unit = { _ -> },
) {
    var selectedMode by remember(mode) { mutableStateOf(mode) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .height(height),
    ) {
        SeasonsMode.entries.forEach { option ->
            ToggleButton(
                enabled = enabled,
                checked = selectedMode == option,
                onCheckedChange = {
                    if (selectedMode != option) {
                        selectedMode = option
                        onModeSelect(option)
                    }
                },
                colors = ToggleButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = TraktTheme.colors.textPrimary,
                    checkedContainerColor = TraktTheme.colors.accent,
                    checkedContentColor = TraktTheme.colors.textPrimaryOnAccent,
                    disabledContentColor = TraktTheme.colors.chipContainer,
                    disabledContainerColor = Color.Transparent,
                ),
                border = when {
                    !enabled -> BorderStroke(
                        width = 1.dp,
                        color = TraktTheme.colors.chipContainer,
                    )
                    selectedMode == option -> BorderStroke(
                        width = 0.dp,
                        color = Color.Transparent,
                    )
                    else -> BorderStroke(
                        width = 1.dp,
                        color = TraktTheme.colors.chipContainer,
                    )
                },
                shapes = when (option) {
                    SeasonsMode.Episodes -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    SeasonsMode.Info -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    SeasonsMode.Reviews -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                modifier = Modifier
                    .weight(1F)
                    .height(height),
            ) {
                Icon(
                    painter = painterResource(
                        when (selectedMode == option) {
                            true -> option.onIcon
                            false -> option.offIcon
                        },
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )

                Text(
                    text = stringResource(option.displayRes),
                    style = TraktTheme.typography.buttonTertiary,
                    modifier = Modifier.padding(start = 5.dp),
                )
            }
        }
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SeasonsMode.entries.forEach {
                SeasonsModeButtons(
                    mode = it,
                )
            }

            SeasonsModeButtons(
                mode = SeasonsMode.Episodes,
                enabled = false,
            )
        }
    }
}
