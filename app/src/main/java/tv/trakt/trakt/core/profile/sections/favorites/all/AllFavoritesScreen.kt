@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile.sections.favorites.all

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.profile.model.FavoriteItem
import tv.trakt.trakt.core.profile.sections.favorites.all.views.AllFavoritesMovieView
import tv.trakt.trakt.core.profile.sections.favorites.all.views.AllFavoritesShowView
import tv.trakt.trakt.core.profile.sections.favorites.context.movie.FavoriteMovieContextSheet
import tv.trakt.trakt.core.profile.sections.favorites.context.show.FavoriteShowContextSheet
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllFavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: AllFavoritesViewModel,
    onNavigateBack: () -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextMovieSheet by remember { mutableStateOf<Movie?>(null) }
    var contextShowSheet by remember { mutableStateOf<Show?>(null) }

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

    AllFavoritesContent(
        state = state,
        modifier = modifier,
        onClick = {
            when (it) {
                is FavoriteItem.ShowItem -> viewModel.navigateToShow(it.show)
                is FavoriteItem.MovieItem -> viewModel.navigateToMovie(it.movie)
            }
        },
        onLongClick = {
            when (it) {
                is FavoriteItem.MovieItem -> contextMovieSheet = it.movie
                is FavoriteItem.ShowItem -> contextShowSheet = it.show
            }
        },
        onFilterClick = { viewModel.setFilter(it) },
        onBackClick = onNavigateBack,
    )

    FavoriteMovieContextSheet(
        movie = contextMovieSheet,
        onDismiss = { contextMovieSheet = null },
    )

    FavoriteShowContextSheet(
        show = contextShowSheet,
        onDismiss = { contextShowSheet = null },
    )
}

@Composable
internal fun AllFavoritesContent(
    state: AllFavoritesState,
    modifier: Modifier = Modifier,
    onClick: (FavoriteItem) -> Unit = {},
    onLongClick: (FavoriteItem) -> Unit = {},
    onFilterClick: (MediaMode) -> Unit = {},
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
            subtitle = stringResource(R.string.text_sort_recently_added),
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            listFilter = state.filter ?: MediaMode.MEDIA,
            contentPadding = contentPadding,
            onFilterClick = onFilterClick,
            onClick = onClick,
            onLongClick = onLongClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun TitleBar(
    subtitle: String,
    modifier: Modifier = Modifier,
) {
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
            title = stringResource(R.string.list_title_favorites),
            subtitle = subtitle,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    listItems: ImmutableList<FavoriteItem>,
    listFilter: MediaMode,
    subtitle: String,
    contentPadding: PaddingValues,
    onClick: (FavoriteItem) -> Unit,
    onLongClick: (FavoriteItem) -> Unit,
    onFilterClick: (MediaMode) -> Unit,
    onBackClick: () -> Unit,
) {
    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item {
            TitleBar(
                subtitle = subtitle,
                modifier = Modifier
                    .padding(top = 3.dp, bottom = 8.dp)
                    .onClick { onBackClick() },
            )
        }

        item {
            ContentFilters(
                watchlistFilter = listFilter,
                onFilterClick = onFilterClick,
            )
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            when (item) {
                is FavoriteItem.ShowItem -> AllFavoritesShowView(
                    item = item,
                    mediaIcon = listFilter == MediaMode.MEDIA,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )

                is FavoriteItem.MovieItem -> AllFavoritesMovieView(
                    item = item,
                    mediaIcon = listFilter == MediaMode.MEDIA,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (listItems.isEmpty()) {
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
    watchlistFilter: MediaMode,
    onFilterClick: (MediaMode) -> Unit,
) {
    MediaModeFilters(
        selected = watchlistFilter,
        onClick = onFilterClick,
        paddingVertical = PaddingValues(
            top = 0.dp,
            bottom = 19.dp,
        ),
    )
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
)
@Composable
private fun Preview() {
    TraktTheme {
        AllFavoritesContent(
            state = AllFavoritesState(),
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
        AllFavoritesContent(
            state = AllFavoritesState(),
        )
    }
}
