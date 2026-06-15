package tv.trakt.trakt.core.profile.sections.activity.all.ui.filters

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileActivityFilters(
    modifier: Modifier = Modifier,
    selected: ProfileActivityFilter? = null,
    unselectedTextVisible: Boolean = true,
    height: Dp = 28.dp,
    paddingHorizontal: PaddingValues = PaddingValues.Zero,
    paddingVertical: PaddingValues = PaddingValues.Zero,
    onClick: (ProfileActivityFilter) -> Unit = {},
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
        for (filter in ProfileActivityFilter.entries) {
            FilterChip(
                selected = selected == filter,
                animated = initialSelected.value != null,
                text = stringResource(filter.displayRes),
                height = height,
                unselectedTextVisible = unselectedTextVisible,
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(FilterChipDefaults.IconSize)
                            .graphicsLayer {
                                translationX = (-1).dp.toPx()
                            },
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
        ProfileActivityFilters(
            selected = ProfileActivityFilter.Ratings,
        )
    }
}
