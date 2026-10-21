package tv.trakt.trakt.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.ui.theme.TraktTheme

private val Options = MediaMode.entries.toImmutableList()

@Composable
internal fun MediaModeButtons(
    modifier: Modifier = Modifier,
    height: Dp = TraktTheme.size.trackHeight,
    mode: MediaMode? = null,
    onModeSelect: (MediaMode) -> Unit = { _ -> },
) {
    var selectedMode by remember(mode) { mutableStateOf(mode) }

    AnimatedVisibility(
        visible = selectedMode != null,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier.height(height),
    ) {
        SegmentedToggle(
            options = Options,
            selected = selectedMode,
            onSelect = { option ->
                selectedMode = option
                onModeSelect(option)
            },
            height = height,
        ) { option, selected ->
            Icon(
                painter = painterResource(
                    when (selected) {
                        true -> option.onIcon
                        false -> option.offIcon
                    },
                ),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )

            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(150, delayMillis = 100)) +
                    expandHorizontally(tween(100, delayMillis = 100)),
                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(100)),
            ) {
                Text(
                    text = stringResource(option.displayRes),
                    style = TraktTheme.typography.buttonSecondary,
                    modifier = Modifier.padding(start = 5.dp),
                )
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
