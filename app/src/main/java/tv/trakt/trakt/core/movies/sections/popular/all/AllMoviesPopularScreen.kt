@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.movies.sections.popular.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.ui.AllMoviesListView
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllMoviesPopularScreen(
    viewModel: AllMoviesPopularViewModel,
    onNavigateBack: () -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextSheet by remember { mutableStateOf<Movie?>(null) }

    AllMoviesPopularScreenContent(
        state = state,
        onItemClick = { onMovieClick(it.ids.trakt) },
        onItemLongClick = {
            if (!state.loading.isLoading) {
                contextSheet = it
            }
        },
        onBackClick = onNavigateBack,
        onLoadMoreData = {
            viewModel.loadMoreData()
        },
    )

    MovieContextSheet(
        movie = contextSheet,
        onDismiss = { contextSheet = null },
    )
}

@Composable
private fun AllMoviesPopularScreenContent(
    state: AllMoviesPopularState,
    modifier: Modifier = Modifier,
    onLoadMoreData: () -> Unit = {},
    onItemClick: (Movie) -> Unit = {},
    onItemLongClick: (Movie) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val headerState = rememberHeaderState()
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(headerState.connection),
    ) {
        ScrollableBackdropImage(
            imageUrl = state.backgroundUrl,
            scrollState = listState,
        )

        AllMoviesListView(
            state = listState,
            items = state.items ?: emptyList<Movie>().toImmutableList(),
            loading = state.loadingMore.isLoading || state.loading.isLoading,
            title = {
                TitleBar(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .onClick { onBackClick() },
                )
            },
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick,
            onTopOfList = {
                headerState.resetScrolled()
            },
            onEndOfList = {
                onLoadMoreData()
            },
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        Text(
            text = stringResource(R.string.list_title_popular_movies),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
        )
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
        AllMoviesPopularScreenContent(
            state = AllMoviesPopularState(),
        )
    }
}
