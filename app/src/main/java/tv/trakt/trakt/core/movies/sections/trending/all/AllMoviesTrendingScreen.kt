@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.movies.sections.trending.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.movies.model.WatchersMovie
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllMoviesScreen(
    viewModel: AllMoviesTrendingViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllMoviesScreenContent(
        state = state,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun AllMoviesScreenContent(
    state: AllMoviesTrendingState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val headerState = rememberHeaderState()
    val gridState = rememberLazyGridState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val isScrolledToTop by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex == 0 &&
                gridState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            headerState.resetScrolled()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(headerState.connection),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
        )

        val topPadding = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()

        val contentPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
            top = topPadding,
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2)
                .plus(TraktTheme.spacing.mainPageBottomSpace),
        )

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
            verticalArrangement = spacedBy(0.dp),
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TitleBar(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .onClick(onBackClick),
                )
            }

            items(
                items = state.items ?: emptyList<WatchersMovie>().toImmutableList(),
                key = { it.movie.ids.trakt.value },
            ) { item ->
                VerticalMediaCard(
                    title = item.movie.title,
                    imageUrl = item.movie.images?.getPosterUrl(),
                    chipContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.chipsSpacing),
                        ) {
                            item.movie.released?.let {
                                InfoChip(
                                    text = it.year.toString(),
                                )
                            }
                            item.movie.runtime?.inWholeMinutes?.let {
                                val runtimeString = remember(item.movie.runtime) {
                                    it.durationFormat()
                                }
                                InfoChip(
                                    text = runtimeString,
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainGridHorizontalSpace * 2)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }
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
            text = stringResource(R.string.list_title_trending_movies),
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
        AllMoviesScreenContent(
            state = AllMoviesTrendingState(),
        )
    }
}
