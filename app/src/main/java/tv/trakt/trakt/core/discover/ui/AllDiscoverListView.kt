package tv.trakt.trakt.core.discover.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverItem.MovieItem
import tv.trakt.trakt.core.discover.model.DiscoverItem.ShowItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AllDiscoverListView(
    state: LazyListState,
    filter: MediaMode,
    items: ImmutableList<DiscoverItem>,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    loading: Boolean = false,
    onItemClick: (DiscoverItem) -> Unit = {},
    onItemLongClick: (DiscoverItem) -> Unit = {},
    onTopOfList: () -> Unit = {},
    onEndOfList: () -> Unit = {},
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

    val isScrolledToBottom by remember(items.size) {
        derivedStateOf {
            state.firstVisibleItemIndex >= (items.size - 5)
        }
    }

    val isScrolledToTop by remember {
        derivedStateOf {
            state.firstVisibleItemIndex == 0 &&
                state.firstVisibleItemScrollOffset == 0
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
        state = state,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        if (title != null) {
            item { title() }
        }

        listItems(
            items = items,
            mediaIcon = (filter == MediaMode.MEDIA),
            onClick = onItemClick,
            onLongClick = onItemLongClick,
        )

        if (loading) {
            item {
                FilmProgressIndicator(
                    size = 32.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun LazyListScope.listItems(
    items: ImmutableList<DiscoverItem>,
    mediaIcon: Boolean,
    onClick: ((DiscoverItem) -> Unit)? = null,
    onLongClick: ((DiscoverItem) -> Unit)? = null,
) {
    items(
        items = items,
        key = { it.key },
    ) { item ->
        when (item) {
            is ShowItem -> ShowListItem(
                item = item,
                mediaIcon = mediaIcon,
                onClick = onClick?.let { { it(item) } },
                onLongClick = onLongClick?.let { { it(item) } },
                modifier = Modifier
                    .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                    .animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
            )
            is MovieItem -> MovieListItem(
                item = item,
                mediaIcon = mediaIcon,
                onClick = onClick?.let { { it(item) } },
                onLongClick = onLongClick?.let { { it(item) } },
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

@Composable
private fun ShowListItem(
    item: ShowItem,
    modifier: Modifier = Modifier,
    mediaIcon: Boolean,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val genresText = remember(item.show.genres) {
        item.show.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    PanelMediaCard(
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        footerContent = {
            ShowMetaFooter(
                show = item.show,
                mediaIcon = mediaIcon,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun MovieListItem(
    item: MovieItem,
    modifier: Modifier = Modifier,
    mediaIcon: Boolean,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val genresText = remember(item.movie.genres) {
        item.movie.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    PanelMediaCard(
        title = item.movie.title,
        titleOriginal = item.movie.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        footerContent = {
            MovieMetaFooter(
                movie = item.movie,
                mediaIcon = mediaIcon,
            )
        },
        modifier = modifier,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun AllDiscoverListViewPreview() {
    TraktTheme {
        AllDiscoverListView(
            state = LazyListState(),
            filter = MediaMode.MEDIA,
            items = listOf(
                ShowItem(
                    show = PreviewData.show1,
                ),
                MovieItem(
                    movie = PreviewData.movie1,
                ),
            ).toImmutableList(),
        )
    }
}
