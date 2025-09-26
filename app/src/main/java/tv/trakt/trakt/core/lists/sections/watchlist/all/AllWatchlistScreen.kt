@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.sections.watchlist.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.ui.theme.colors.White
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.ShowItem
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllWatchlistScreen(
    modifier: Modifier = Modifier,
    viewModel: AllWatchlistViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllWatchlistContent(
        state = state,
        modifier = modifier,
        onBackClick = onNavigateBack,
    )
}

@Composable
internal fun AllWatchlistContent(
    state: AllWatchlistState,
    modifier: Modifier = Modifier,
    onTopOfList: () -> Unit = {},
    onEndOfList: () -> Unit = {},
    onLongClick: (WatchlistItem) -> Unit = {},
    onCheckClick: (WatchlistItem) -> Unit = {},
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
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            contentPadding = contentPadding,
            onTopOfList = onTopOfList,
            onEndOfList = onEndOfList,
            onBackClick = onBackClick,
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
        TraktHeader(
            title = stringResource(R.string.page_title_watchlist),
            subtitle = stringResource(R.string.text_sort_recently_added),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listItems: ImmutableList<WatchlistItem>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onTopOfList: () -> Unit,
    onEndOfList: () -> Unit,
    onBackClick: () -> Unit,
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
                    .padding(bottom = 8.dp)
                    .onClick(onBackClick),
            )
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            when (item) {
                is MovieItem -> ContentMovieListItem(
                    item = item,
                    onLongClick = { },
                    onCheckClick = { },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
                is ShowItem -> ContentShowListItem(
                    item = item,
                    onLongClick = { },
                    onCheckClick = { },
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
}

@Composable
private fun ContentShowListItem(
    item: ShowItem,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genresText = remember(item.show.genres) {
        item.show.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(THUMB),
        onLongClick = onLongClick,
        footerContent = {
            Row(
                horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
                verticalAlignment = CenterVertically,
            ) {
                val epsString = stringResource(R.string.tag_text_number_of_episodes, item.show.airedEpisodes)
                val metaString = remember {
                    val separator = "  •  "
                    buildString {
                        item.released?.let {
                            append(it.year)
                        }
                        if (item.show.airedEpisodes > 0) {
                            if (isNotEmpty()) append(separator)
                            append(epsString)
                        }
                        if (!item.show.certification.isNullOrBlank()) {
                            if (isNotEmpty()) append(separator)
                            append(item.show.certification)
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shows_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer {
                                translationY = -1.dp.toPx()
                            },
                    )
                    Text(
                        text = metaString,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }

                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(4.dp),
                ) {
                    val grayFilter = remember {
                        ColorFilter.colorMatrix(
                            ColorMatrix().apply {
                                setToSaturation(0F)
                            },
                        )
                    }
                    val whiteFilter = remember {
                        ColorFilter.tint(White)
                    }

                    Spacer(modifier = Modifier.weight(1F))

                    Image(
                        painter = painterResource(R.drawable.ic_trakt_icon),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        colorFilter = if (item.show.rating.rating > 0) whiteFilter else grayFilter,
                    )
                    Text(
                        text = if (item.show.rating.rating > 0) "${item.show.rating.ratingPercent}%" else "-",
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }
            }
        },
    )
}

@Composable
private fun ContentMovieListItem(
    item: MovieItem,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genresText = remember(item.movie.genres) {
        item.movie.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    PanelMediaCard(
        modifier = modifier,
        title = item.movie.title,
        titleOriginal = item.movie.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(THUMB),
        onLongClick = onLongClick,
        footerContent = {
            Row(
                horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
                verticalAlignment = CenterVertically,
            ) {
                val metaString = remember {
                    val separator = "  •  "
                    buildString {
                        item.released?.let {
                            append(it.year)
                        }
                        item.movie.runtime?.let {
                            if (isNotEmpty()) append(separator)
                            append(it.inWholeMinutes.durationFormat())
                        }
                        if (!item.movie.certification.isNullOrBlank()) {
                            if (isNotEmpty()) append(separator)
                            append(item.movie.certification)
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_movies_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = metaString,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }

                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(4.dp),
                ) {
                    val grayFilter = remember {
                        ColorFilter.colorMatrix(
                            ColorMatrix().apply {
                                setToSaturation(0F)
                            },
                        )
                    }
                    val whiteFilter = remember {
                        ColorFilter.tint(White)
                    }

                    Spacer(modifier = Modifier.weight(1F))

                    Image(
                        painter = painterResource(R.drawable.ic_trakt_icon),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        colorFilter = if (item.movie.rating.rating > 0) whiteFilter else grayFilter,
                    )
                    Text(
                        text = if (item.movie.rating.rating > 0) "${item.movie.rating.ratingPercent}%" else "-",
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }
            }
        },
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
        AllWatchlistContent(
            state = AllWatchlistState(),
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
        AllWatchlistContent(
            state = AllWatchlistState(),
        )
    }
}
