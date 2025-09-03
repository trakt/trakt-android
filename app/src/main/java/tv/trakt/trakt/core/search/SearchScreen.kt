package tv.trakt.trakt.core.search

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel,
    searchInput: SearchInput,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onProfileClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val localBottomBarVisibility = LocalBottomBarVisibility.current
    LaunchedEffect(Unit) {
        localBottomBarVisibility.value = true
    }

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it.ids.trakt)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it.ids.trakt)
        }
    }

    LaunchedEffect(searchInput) {
        viewModel.updateSearch(searchInput)
    }

    SearchScreenContent(
        state = state,
        onSearchQuery = viewModel::searchQuery,
        onShowClick = { viewModel.navigateToShow(it) },
        onMovieClick = { viewModel.navigateToMovie(it) },
        onProfileClick = onProfileClick,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchScreenContent(
    state: SearchState,
    modifier: Modifier = Modifier,
    onSearchQuery: (String) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val headerState = rememberHeaderState()
    val lazyListState = rememberLazyGridState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

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
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(headerState.connection),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
            modifier = Modifier.graphicsLayer {
                if (lazyListState.firstVisibleItemIndex == 0) {
                    translationY = (-0.75F * lazyListState.firstVisibleItemScrollOffset)
                } else {
                    alpha = 0F
                }
            },
        )

        ContentList(
            recentItems = (state.recentsResult?.items ?: emptyList()).toImmutableList(),
            popularItems = (state.popularResults?.items ?: emptyList()).toImmutableList(),
            listState = lazyListState,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
        )

        SearchScreenHeader(
            state = state,
            headerState = headerState,
            isScrolledToTop = isScrolledToTop,
            onProfileClick = onProfileClick,
        )
    }
}

@Composable
private fun ContentList(
    recentItems: ImmutableList<SearchItem>,
    popularItems: ImmutableList<SearchItem>,
    listState: LazyGridState = rememberLazyGridState(),
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
) {
    val topPadding = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()
        .plus(TraktTheme.spacing.mainPageTopSpace)

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Fixed(3),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
        contentPadding = contentPadding,
        overscrollEffect = null,
    ) {
        if (recentItems.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Recently Searched", // TODO
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading5,
                    modifier = Modifier.padding(top = topPadding),
                )
            }

            items(
                count = recentItems.size,
                key = { index -> "${recentItems[index].key}_recent" },
            ) { index ->
                ContentListItem(
                    item = recentItems[index],
                    onShowClick = onShowClick,
                    onMovieClick = onMovieClick,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .animateItem(
                            fadeInSpec = tween(200),
                            fadeOutSpec = tween(200),
                        ),
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Popular Searches", // TODO
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
                modifier = Modifier.padding(top = if (recentItems.isEmpty()) topPadding else 10.dp),
            )
        }

        items(
            count = popularItems.size,
            key = { index -> "${popularItems[index].key}_popular" },
        ) { index ->
            ContentListItem(
                item = popularItems[index],
                onShowClick = onShowClick,
                onMovieClick = onMovieClick,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .animateItem(
                        fadeInSpec = tween(200),
                        fadeOutSpec = tween(200),
                    ),
            )
        }
    }
}

@Composable
private fun ContentListItem(
    item: SearchItem,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
) {
    when (item) {
        is SearchItem.Show -> {
            VerticalMediaCard(
                title = item.show.title,
                imageUrl = item.show.images?.getPosterUrl(),
                chipContent = {
                    Row(
                        horizontalArrangement = spacedBy(5.dp),
                    ) {
//                        InfoChip(
//                            text = "",
//                            iconPainter = painterResource(R.drawable.ic_shows_off),
//                            iconPadding = 1.dp,
//                        )
                        item.show.released?.year?.let {
                            InfoChip(
                                text = it.toString(),
                                iconPainter = painterResource(R.drawable.ic_shows_off),
                                iconPadding = 2.dp,
                            )
                        }
                    }
                },
                onClick = { onShowClick(item.show) },
                modifier = modifier,
            )
        }
        is SearchItem.Movie -> {
            VerticalMediaCard(
                title = item.movie.title,
                imageUrl = item.movie.images?.getPosterUrl(),
                chipContent = {
                    Row(
                        horizontalArrangement = spacedBy(5.dp),
                    ) {
//                        InfoChip(
//                            text = "",
//                            iconPainter = painterResource(R.drawable.ic_movies_off),
//                            iconPadding = 1.dp,
//                        )
                        item.movie.released?.year?.let {
                            InfoChip(
                                text = it.toString(),
                                iconPainter = painterResource(R.drawable.ic_movies_off),
                                iconPadding = 1.dp,
                            )
                        }
                    }
                },
                onClick = { onMovieClick(item.movie) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun SearchScreenHeader(
    state: SearchState,
    headerState: ScreenHeaderState,
    isScrolledToTop: Boolean,
    onProfileClick: () -> Unit,
) {
    val userState = remember(state.user) {
        val loadingDone = state.user.loading == DONE
        val userNotNull = state.user.user != null
        loadingDone to userNotNull
    }

    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showVip = headerState.startScrolled,
        showProfile = userState.first && userState.second,
        showJoinTrakt = userState.first && !userState.second,
        userVip = state.user.user?.isAnyVip ?: false,
        userAvatar = state.user.user?.images?.avatar?.full,
        onJoinClick = onProfileClick,
        onProfileClick = onProfileClick,
        modifier = Modifier.offset {
            IntOffset(0, headerState.connection.barOffset.fastRoundToInt())
        },
    )
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
            state = SearchState(),
        )
    }
}
