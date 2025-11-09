package tv.trakt.trakt.core.search.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchFiltersList(
    modifier: Modifier = Modifier,
    selectedFilter: SearchFilter = SearchFilter.MEDIA,
    onFilterClick: (SearchFilter) -> Unit = {},
) {
    FilterChipGroup(
        horizontalArrangement = spacedBy(6.dp, CenterHorizontally),
        paddingHorizontal = PaddingValues.Zero,
        paddingVertical = PaddingValues.Zero,
        modifier = modifier,
    ) {
        for (filter in SearchFilter.entries) {
            FilterChip(
                selected = selectedFilter == filter,
                text = stringResource(filter.displayRes),
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                },
                onClick = {
                    if (selectedFilter != filter) {
                        onFilterClick(filter)
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun Preview1() {
    TraktTheme {
        SearchFiltersList(
            selectedFilter = SearchFilter.MEDIA,
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        SearchFiltersList(
            selectedFilter = SearchFilter.SHOWS,
        )
    }
}

@Preview
@Composable
private fun Preview3() {
    TraktTheme {
        SearchFiltersList(
            selectedFilter = SearchFilter.MOVIES,
        )
    }
}
