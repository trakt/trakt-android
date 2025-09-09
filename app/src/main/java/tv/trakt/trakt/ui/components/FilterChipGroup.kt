package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun FilterChipGroup(
    modifier: Modifier = Modifier,
    paddingVertical: PaddingValues = PaddingValues(top = 13.dp, bottom = 15.dp),
    paddingHorizontal: PaddingValues = PaddingValues(start = 0.dp, end = 0.dp),
    horizontalArrangement: Arrangement.Horizontal = spacedBy(6.dp),
    content: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .padding(paddingVertical)
            .padding(paddingHorizontal),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
