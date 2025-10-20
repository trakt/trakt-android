@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.home.sections.activity.all.personal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.sections.activity.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.all.views.AllActivityEpisodeItem
import tv.trakt.trakt.core.home.sections.activity.all.views.AllActivityMovieItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.core.home.sections.activity.sheets.HomeActivityItemSheet
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextContent
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllActivityPersonalScreen(
    modifier: Modifier = Modifier,
    viewModel: AllActivityPersonalViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onNavigateToShow(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let {
            onNavigateToEpisode(it.first, it.second)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onNavigateToMovie(it)
            viewModel.clearNavigation()
        }
    }

    var contextSheet by remember { mutableStateOf<HomeActivityItem?>(null) }

    AllActivityPersonalContent(
        state = state,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onLoadMore = {
            viewModel.loadMoreData()
        },
        onLongClick = {
            if (!state.loading.isLoading && !state.loadingMore.isLoading) {
                contextSheet = it
            }
        },
        onShowClick = { episodeItem ->
            viewModel.navigateToShow(episodeItem.show)
        },
        onEpisodeClick = { episodeItem ->
            viewModel.navigateToEpisode(
                episodeItem.show,
                episodeItem.episode,
            )
        },
        onMovieClick = { movie ->
            viewModel.navigateToMovie(movie)
        },
    )

    HomeActivityItemSheet(
        sheetItem = contextSheet,
        onDismiss = { contextSheet = null },
        onPlayRemoved = { viewModel.removeItem(it) },
    )
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
            text = stringResource(R.string.page_title_recently_watched),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
        )
    }
}

@Composable
internal fun AllActivityPersonalContent(
    state: AllActivityState,
    modifier: Modifier = Modifier,
    onLongClick: (HomeActivityItem) -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onShowClick: (EpisodeItem) -> Unit = {},
    onEpisodeClick: (EpisodeItem) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
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
        val contentPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            imageUrl = state.backgroundUrl,
            scrollState = listState,
        )

        ContentList(
            listState = listState,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            contentPadding = contentPadding,
            loadingMore = state.loadingMore.isLoading,
            onTopOfList = { headerState.resetScrolled() },
            onEndOfList = onLoadMore,
            onLongClick = onLongClick,
            onBackClick = onBackClick,
            onShowClick = onShowClick,
            onEpisodeClick = onEpisodeClick,
            onMovieClick = onMovieClick,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listItems: ImmutableList<HomeActivityItem>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    loadingMore: Boolean,
    onTopOfList: () -> Unit,
    onEndOfList: () -> Unit,
    onLongClick: (HomeActivityItem) -> Unit,
    onBackClick: () -> Unit,
    onShowClick: (EpisodeItem) -> Unit,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onMovieClick: (Movie) -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= (listItems.size - 5)
        }
    }

    val isScrolledToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            onTopOfList()
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item {
            TitleBar(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .onClick {
                        onBackClick()
                    },
            )
        }

        items(
            items = listItems,
            key = { it.id },
        ) { item ->
            when (item) {
                is MovieItem -> {
                    AllActivityMovieItem(
                        item = item,
                        onClick = {
                            onMovieClick(item.movie)
                        },
                        onLongClick = { onLongClick(item) },
                        moreButton = true,
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
                is EpisodeItem -> {
                    AllActivityEpisodeItem(
                        item = item,
                        onClick = { onEpisodeClick(item) },
                        onShowClick = { onShowClick(item) },
                        onLongClick = { onLongClick(item) },
                        moreButton = true,
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
            }
        }

        if (loadingMore) {
            item {
                FilmProgressIndicator(
                    size = 32.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
        AllHomeUpNextContent(
            state = AllHomeUpNextState(
                loading = LoadingState.DONE,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        AllHomeUpNextContent(
            state = AllHomeUpNextState(
                loading = LOADING,
            ),
        )
    }
}
