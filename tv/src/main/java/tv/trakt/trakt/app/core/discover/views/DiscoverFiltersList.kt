package tv.trakt.trakt.app.core.discover.views

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.common.ui.chips.FilterChip
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.MediaMode

@Composable
internal fun DiscoverFiltersList(
    selectedMode: MediaMode,
    modifier: Modifier = Modifier,
    selectedFocusRequester: FocusRequester = remember { FocusRequester() },
    downFocusRequester: FocusRequester? = null,
    onModeClick: (MediaMode) -> Unit = {},
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier.focusGroup(),
    ) {
        MediaMode.entries.forEach { mode ->
            FilterChip(
                text = stringResource(mode.displayRes),
                icon = painterResource(mode.offIcon),
                selected = mode == selectedMode,
                modifier = Modifier
                    .focusProperties {
                        downFocusRequester?.let { down = it }
                    }
                    .then(
                        if (mode == selectedMode) Modifier.focusRequester(selectedFocusRequester) else Modifier,
                    ),
                onClick = {
                    if (mode != selectedMode) {
                        onModeClick(mode)
                    }
                },
            )
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        DiscoverFiltersList(
            selectedMode = MediaMode.Media,
        )
    }
}
