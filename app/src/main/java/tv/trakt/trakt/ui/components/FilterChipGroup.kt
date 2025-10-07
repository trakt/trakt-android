package tv.trakt.trakt.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun FilterChipGroup(
    modifier: Modifier = Modifier,
    paddingVertical: PaddingValues = PaddingValues(top = 13.dp, bottom = 15.dp),
    paddingHorizontal: PaddingValues = PaddingValues(start = 0.dp, end = 0.dp),
    horizontalArrangement: Arrangement.Horizontal = spacedBy(TraktTheme.spacing.filterChipsSpace),
    content: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .horizontalScroll(
                state = rememberScrollState(),
                overscrollEffect = null,
            )
            .padding(paddingVertical)
            .padding(paddingHorizontal),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
