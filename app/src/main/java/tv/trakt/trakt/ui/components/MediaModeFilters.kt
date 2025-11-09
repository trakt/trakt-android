package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MediaModeFilters(
    modifier: Modifier = Modifier,
    mode: MediaMode? = null,
    paddingVertical: PaddingValues = PaddingValues.Zero,
    onClick: (MediaMode) -> Unit = { _ -> },
) {
    FilterChipGroup(
        paddingHorizontal = PaddingValues.Zero,
        paddingVertical = paddingVertical,
        modifier = modifier,
    ) {
        for (filter in MediaMode.entries) {
            FilterChip(
                selected = mode == filter,
                text = stringResource(filter.label),
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.offIcon),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(16.dp),
                    )
                },
                onClick = {
                    onClick(filter)
                },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(8.dp),
        ) {
            MediaModeFilters(
                mode = null,
            )
            for (mode in MediaMode.entries) {
                MediaModeFilters(
                    mode = mode,
                )
            }
        }
    }
}
