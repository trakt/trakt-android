package tv.trakt.trakt.app.core.search.views

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
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
import tv.trakt.trakt.common.core.search.model.SearchFilter

@Composable
internal fun SearchFiltersList(
    selectedFilter: SearchFilter,
    modifier: Modifier = Modifier,
    firstFocusRequester: FocusRequester = remember { FocusRequester() },
    downFocusRequester: () -> FocusRequester = { FocusRequester.Default },
    onFilterClick: (SearchFilter) -> Unit = {},
) {
    Row(
        horizontalArrangement = spacedBy(8.dp, CenterHorizontally),
        modifier = modifier
            .fillMaxWidth()
            .focusGroup(),
    ) {
        SearchFilter.entries.forEachIndexed { index, filter ->
            FilterChip(
                text = stringResource(filter.displayRes),
                icon = painterResource(filter.iconRes),
                selected = filter == selectedFilter,
                modifier = Modifier
                    .focusProperties {
                        down = downFocusRequester()
                    }
                    .then(
                        if (index == 0) Modifier.focusRequester(firstFocusRequester) else Modifier,
                    ),
                onClick = {
                    if (filter != selectedFilter) {
                        onFilterClick(filter)
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
        SearchFiltersList(
            selectedFilter = SearchFilter.MEDIA,
        )
    }
}
