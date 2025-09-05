package tv.trakt.trakt.core.search.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchFiltersList(
    modifier: Modifier = Modifier,
    selectedFilter: SearchFilter = SearchFilter.MEDIA,
    onFilterClick: (SearchFilter) -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        SearchFilter.entries.forEach {
            FilterChip(
                selected = selectedFilter == it,
                text = stringResource(it.displayRes),
                leadingIcon = {
                    Icon(
                        painter = painterResource(it.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
                onClick = {
                    if (selectedFilter != it) {
                        onFilterClick(it)
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
