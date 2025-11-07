@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package tv.trakt.trakt.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.ui.theme.TraktTheme

private val Options = MediaMode.entries

@Composable
internal fun MediaModeButtons(
    modifier: Modifier = Modifier,
    height: Dp = 35.dp,
    mode: MediaMode? = null,
    onModeSelect: (MediaMode) -> Unit = { _ -> },
) {
    var selectedMode by remember { mutableStateOf(mode) }

    AnimatedVisibility(
        visible = selectedMode != null,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier.height(height),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Options.forEach { option ->
                ToggleButton(
                    checked = selectedMode == option,
                    onCheckedChange = {
                        if (selectedMode != option) {
                            selectedMode = option
                            onModeSelect(option)
                        }
                    },
                    colors = toggleButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = TraktTheme.colors.textPrimary,
                        checkedContainerColor = TraktTheme.colors.accent,
                        checkedContentColor = TraktTheme.colors.textPrimary,
                    ),
                    border = when {
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
                        MediaMode.MEDIA -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        MediaMode.SHOWS -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        MediaMode.MOVIES -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    },
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                    ),
                    modifier = Modifier.height(height),
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

                    Spacer(Modifier.size(5.dp))

                    Text(
                        text = stringResource(option.label),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.buttonTertiary,
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Options.forEach {
                MediaModeButtons(
                    mode = it,
                )
            }
        }
    }
}
