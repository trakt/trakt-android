package tv.trakt.trakt.core.calendar.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme

/** Release-type filter row shared by the weekly and monthly calendars. */
@Composable
internal fun CalendarTypeChips(
    selected: ReleaseType,
    modifier: Modifier = Modifier,
    onTypeClick: (ReleaseType) -> Unit = {},
) {
    FilterChipGroup(
        paddingVertical = PaddingValues.Zero,
        modifier = modifier,
    ) {
        for (type in ReleaseType.entries) {
            FilterChip(
                selected = selected == type,
                text = stringResource(type.textRes),
                leadingContent = {
                    Icon(
                        painter = painterResource(type.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(type.iconSize),
                    )
                },
                onClick = { onTypeClick(type) },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CalendarTypeChips(selected = ReleaseType.All)
    }
}
