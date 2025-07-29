package tv.trakt.trakt.core.shows

import ShowsTrendingView
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.sections.anticipated.ShowsAnticipatedView
import tv.trakt.trakt.core.shows.sections.hot.ShowsHotView
import tv.trakt.trakt.core.shows.sections.popular.ShowsPopularView
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
    val topPadding =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 32.dp

    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        .plus(TraktTheme.size.navigationBarHeight)
        .plus(TraktTheme.spacing.mainPageBottomSpace)

    val sectionPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
    )

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            verticalArrangement = spacedBy(TraktTheme.spacing.mainSectionVerticalSpace),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = bottomPadding,
            ),
        ) {
            item {
                ShowsTrendingView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }
            item {
                ShowsHotView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }
            item {
                ShowsAnticipatedView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }
            item {
                ShowsPopularView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_6",
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
