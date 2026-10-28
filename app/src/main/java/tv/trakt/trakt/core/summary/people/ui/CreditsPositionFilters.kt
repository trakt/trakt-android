package tv.trakt.trakt.core.summary.people.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CreditsPositionFilters(
    modifier: Modifier = Modifier,
    height: Dp = 28.dp,
    filters: Collection<String>,
    selected: String?,
    paddingHorizontal: PaddingValues = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
    ),
    paddingVertical: PaddingValues = PaddingValues(bottom = 18.dp),
    onSelected: (String) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = paddingHorizontal,
        paddingVertical = paddingVertical,
        modifier = modifier,
    ) {
        for (filter in filters) {
            FilterChip(
                height = height,
                text = filter.replaceFirstChar { it.uppercase() },
                selected = selected == filter,
                onClick = {
                    onSelected(filter)
                },
            )
        }
    }
}
