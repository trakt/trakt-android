import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.R
import tv.trakt.trakt.core.shows.sections.trending.ShowsTrendingState
import tv.trakt.trakt.core.shows.sections.trending.ShowsTrendingViewModel
import tv.trakt.trakt.ui.common.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowsTrendingView(
    modifier: Modifier = Modifier,
    viewModel: ShowsTrendingViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowsTrendingContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
    )
}

@Composable
internal fun ShowsTrendingContent(
    state: ShowsTrendingState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.header_trending),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
            Text(
                text = stringResource(R.string.view_all).uppercase(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.buttonTertiary,
            )
        }

        ContentLoadingList(
            contentPadding = contentPadding,
        )
    }
}

@Composable
private fun ContentLoadingList(contentPadding: PaddingValues) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard()
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ShowsTrendingContent(
            state = ShowsTrendingState(),
        )
    }
}
