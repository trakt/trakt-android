package tv.trakt.trakt.core.movies

import android.util.Log
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.sections.anticipated.MoviesAnticipatedView
import tv.trakt.trakt.core.movies.sections.hot.MoviesHotView
import tv.trakt.trakt.core.movies.sections.popular.MoviesPopularView
import tv.trakt.trakt.core.movies.sections.trending.MoviesTrendingView
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MoviesScreen(
    viewModel: MoviesViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MoviesScreenContent(
        state = state,
        onMovieClick = onNavigateToMovie,
    )
}

@Composable
private fun MoviesScreenContent(
    state: MoviesState,
    modifier: Modifier = Modifier,
    onMovieClick: (TraktId) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val headerState = rememberHeaderState()

    val isScrolledToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            headerState.resetScrolled()
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(headerState.connection),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
            modifier = Modifier.graphicsLayer {
                if (lazyListState.firstVisibleItemIndex == 0) {
                    Log.d("MoviesScreen", "MESSI")
                    translationY = (-0.75F * lazyListState.firstVisibleItemScrollOffset)
                } else {
                    alpha = 0F
                }
            },
        )

        val listPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding()
                .plus(TraktTheme.spacing.mainPageTopSpace),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight)
                .plus(TraktTheme.spacing.mainPageBottomSpace),
        )

        val sectionPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
        )

        LazyColumn(
            state = lazyListState,
            overscrollEffect = null,
            verticalArrangement = spacedBy(TraktTheme.spacing.mainSectionVerticalSpace),
            contentPadding = listPadding,
        ) {
            item {
                MoviesTrendingView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }

            item {
                MoviesHotView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }

            item {
                MoviesAnticipatedView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }

            item {
                MoviesPopularView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                )
            }
        }

        HeaderBar(
            containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
            showVip = headerState.startScrolled,
            modifier = Modifier
                .offset {
                    IntOffset(0, headerState.connection.barOffset.fastRoundToInt())
                },
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
        MoviesScreenContent(
            state = MoviesState(),
            onMovieClick = {},
        )
    }
}
