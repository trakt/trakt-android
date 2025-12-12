package tv.trakt.trakt.ui.components.switch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.CustomTheme
import tv.trakt.trakt.ui.theme.model.toTraktDarkColors

@Composable
internal fun TraktThemeSwitch(
    theme: CustomTheme,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scope = rememberCoroutineScope()

    val colors = remember(theme.colors) {
        theme.colors?.toTraktDarkColors()
    }

    var isChecked by remember { mutableStateOf(checked) }

    Switch(
        checked = isChecked,
        onCheckedChange = {
            isChecked = it
            scope.launch {
                delay(200)
                onCheckedChange?.invoke(it)
            }
        },
        enabled = enabled,
        thumbContent = {
            when {
                theme.type == "christmas" -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_christmas_tree),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                translationY = -0.25.dp.toPx()
                            },
                    )
                }

                else -> {
                    Spacer(
                        modifier = Modifier
                            .rotate(if (checked) 45F else -45F)
                            .background(Color.White)
                            .size(2.dp, 8.dp),
                    )
                }
            }
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors?.switchThumbChecked ?: TraktTheme.colors.switchThumbChecked,
            uncheckedThumbColor = colors?.switchThumbUnchecked ?: TraktTheme.colors.switchThumbUnchecked,
            checkedTrackColor = colors?.switchContainerChecked ?: TraktTheme.colors.switchContainerChecked,
            uncheckedTrackColor = colors?.switchContainerUnchecked ?: TraktTheme.colors.switchContainerUnchecked,
            checkedBorderColor = colors?.switchContainerChecked ?: TraktTheme.colors.switchContainerChecked,
            uncheckedBorderColor = colors?.switchContainerUnchecked ?: TraktTheme.colors.switchContainerUnchecked,
            // Disabled
            disabledUncheckedTrackColor = Shade800,
            disabledUncheckedThumbColor = Shade500,
            disabledCheckedBorderColor = Shade800,
            disabledUncheckedBorderColor = Shade800,
            disabledCheckedTrackColor = Shade800,
            disabledCheckedThumbColor = Shade500,
        ),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        TraktThemeSwitch(
            theme = CustomTheme(
                id = "christmas",
                type = "holiday",
                backgroundImageUrl = null,
                colors = null,
                filters = null,
            ),
            checked = true,
            onCheckedChange = {},
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        TraktThemeSwitch(
            theme = CustomTheme(
                id = "christmas-2025",
                type = "christmas",
                backgroundImageUrl = null,
                colors = null,
                filters = null,
            ),
            checked = false,
            onCheckedChange = {},
        )
    }
}

@Preview
@Composable
private fun Preview3() {
    TraktTheme {
        TraktThemeSwitch(
            theme = CustomTheme(
                id = "christmas-2025",
                type = "christmas",
                backgroundImageUrl = null,
                colors = null,
                filters = null,
            ),
            checked = true,
            enabled = false,
            onCheckedChange = {},
        )
    }
}

@Preview
@Composable
private fun Preview4() {
    TraktTheme {
        TraktThemeSwitch(
            theme = CustomTheme(
                id = "christmas-2025",
                type = "christmas",
                backgroundImageUrl = null,
                colors = null,
                filters = null,
            ),
            checked = false,
            enabled = false,
            onCheckedChange = {},
        )
    }
}
