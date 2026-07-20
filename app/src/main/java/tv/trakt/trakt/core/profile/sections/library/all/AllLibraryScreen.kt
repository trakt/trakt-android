@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile.sections.library.all

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.core.library.LibraryItem
import tv.trakt.trakt.common.core.library.LibraryItem.EpisodeItem
import tv.trakt.trakt.common.core.library.LibraryItem.MovieItem
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.library.model.LibraryFilter
import tv.trakt.trakt.core.profile.sections.library.all.views.AllLibraryEpisodeView
import tv.trakt.trakt.core.profile.sections.library.all.views.AllLibraryMovieView
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllLibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: AllLibraryViewModel,
    onNavigateBack: () -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it)
        }
    }

    AllLibraryContent(
        state = state,
        modifier = modifier,
        onLoadMore = { viewModel.loadMoreData() },
        onClick = {
            scope.launch {
                when (it) {
                    is MovieItem -> {
                        viewModel.onNavigateToMovie(it.movie)
                        onMovieClick(it.movie.ids.trakt)
                    }

                    is EpisodeItem -> {
                        viewModel.onNavigateToShow(it.show)
                        onShowClick(it.show.ids.trakt)
                    }
                }
            }
        },
        onFilterClick = { viewModel.setFilter(it) },
        onBackClick = onNavigateBack,
    )
}

@Composable
internal fun AllLibraryContent(
    state: AllLibraryState,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
    onClick: (LibraryItem) -> Unit = {},
    onFilterClick: (LibraryFilter) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
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
            translation = listScrollConnection.resultOffset,
        )

        ContentList(
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            listFilter = state.filter,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            loadingMore = state.loadingMore.isLoading,
            onEndOfList = onLoadMore,
            onFilterClick = onFilterClick,
            onClick = onClick,
            onBackClick = onBackClick,
        )

        if (state.error != null) {
            Text(
                text = "${
                    stringResource(
                        R.string.error_text_unexpected_error_short,
                    )
                }\n\n${state.error}",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
                maxLines = 10,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(contentPadding),
            )
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
        TraktHeader(
            title = stringResource(R.string.list_title_library),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    listItems: ImmutableList<LibraryItem>,
    listFilter: LibraryFilter?,
    contentPadding: PaddingValues,
    loading: Boolean,
    loadingMore: Boolean,
    onEndOfList: () -> Unit,
    onClick: (LibraryItem) -> Unit,
    onFilterClick: (LibraryFilter) -> Unit,
    onBackClick: () -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= (listItems.size - 5)
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
                    .padding(top = 3.dp)
                    .onClick { onBackClick() },
            )
        }

        item {
            ContentFilters(
                filter = listFilter,
                onFilterClick = onFilterClick,
            )
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            when (item) {
                is EpisodeItem -> AllLibraryEpisodeView(
                    item = item,
                    enabled = !loading,
                    onClick = { onClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )

                is MovieItem -> AllLibraryMovieView(
                    item = item,
                    enabled = !loading,
                    onClick = { onClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (loading && listItems.isEmpty()) {
            items(10) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (loadingMore && listItems.isNotEmpty()) {
            items(1) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (listItems.isEmpty()) {
            item {
                ContentEmptyView(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(200),
                            fadeOutSpec = tween(200),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ContentFilters(
    filter: LibraryFilter?,
    modifier: Modifier = Modifier,
    onFilterClick: (LibraryFilter) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = PaddingValues(horizontal = 0.dp),
        paddingVertical = PaddingValues(top = 0.dp, bottom = 19.dp),
        modifier = modifier,
    ) {
        for (libraryFilter in LibraryFilter.entries) {
            FilterChip(
                selected = libraryFilter == filter,
                height = 32.dp,
                text = stringResource(libraryFilter.displayRes),
                onClick = { onFilterClick(libraryFilter) },
            )
        }
    }
}

@Composable
private fun ContentEmptyView(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = modifier,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "en",
)
@Composable
private fun Preview() {
    TraktTheme {
        AllLibraryContent(
            state = AllLibraryState(),
        )
    }
}
