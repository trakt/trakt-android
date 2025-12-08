package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MediaModeFilters(
    modifier: Modifier = Modifier,
    selected: MediaMode? = null,
    unselectedTextVisible: Boolean = true,
    height: Dp = 28.dp,
    paddingHorizontal: PaddingValues = PaddingValues.Zero,
    paddingVertical: PaddingValues = PaddingValues.Zero,
    onClick: (MediaMode) -> Unit = { _ -> },
) {
    val initialSelected = remember {
        mutableStateOf(selected)
    }

    LaunchedEffect(selected) {
        if (selected != null) {
            initialSelected.value = selected
        }
    }

    FilterChipGroup(
        paddingHorizontal = paddingHorizontal,
        paddingVertical = paddingVertical,
        modifier = modifier,
    ) {
        for (filter in MediaMode.entries) {
            FilterChip(
                selected = selected == filter,
                animated = initialSelected.value != null,
                text = stringResource(filter.displayRes),
                height = height,
                unselectedTextVisible = unselectedTextVisible,
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.offIcon),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(16.dp),
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
            verticalArrangement = spacedBy(12.dp),
        ) {
            MediaModeFilters(
                selected = null,
                unselectedTextVisible = false,
            )
            MediaModeFilters(
                selected = null,
                unselectedTextVisible = true,
            )
            for (mode in MediaMode.entries) {
                MediaModeFilters(
                    selected = mode,
                )
            }
        }
    }
}
