package tv.trakt.trakt.core.summary.shows.features.seasons.all.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsMode
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.ui.components.SegmentedToggle
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SeasonsModeButtons(
    mode: SeasonsMode,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = TraktTheme.size.trackHeight,
    onModeSelect: (SeasonsMode) -> Unit = { _ -> },
) {
    var selectedMode by remember(mode) { mutableStateOf(mode) }

    SegmentedToggle(
        options = SeasonsMode.entries.toImmutableList(),
        selected = selectedMode,
        onSelect = { option ->
            selectedMode = option
            onModeSelect(option)
        },
        enabled = enabled,
        fillWidth = true,
        height = height,
        modifier = modifier,
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

        Text(
            text = stringResource(option.displayRes),
            style = TraktTheme.typography.buttonSecondary,
            modifier = Modifier.padding(start = 5.dp),
        )
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
