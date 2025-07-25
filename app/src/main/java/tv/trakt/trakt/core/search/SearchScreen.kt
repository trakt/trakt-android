package tv.trakt.trakt.core.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.R
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchScreen(onNavigateToSearch: (TraktId) -> Unit) {
    SearchScreenContent(
        onSearchClick = onNavigateToSearch,
    )
}

@Composable
private fun SearchScreenContent(
    modifier: Modifier = Modifier,
    onSearchClick: (TraktId) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Text(
            text = stringResource(R.string.search),
            color = TraktTheme.colors.textPrimary,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Preview(
    device = "id:pixel_9",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        SearchScreenContent(
            onSearchClick = {},
        )
    }
}
