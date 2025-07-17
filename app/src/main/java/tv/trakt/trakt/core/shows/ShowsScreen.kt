package tv.trakt.trakt.core.shows

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
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowsScreen(onNavigateToShow: (TraktId) -> Unit) {
    ShowsScreenContent(
        onShowClick = onNavigateToShow,
    )
}

@Composable
private fun ShowsScreenContent(
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Text(
            text = stringResource(R.string.shows),
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
        ShowsScreenContent(
            onShowClick = {},
        )
    }
}
