package tv.trakt.trakt.core.discover.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverItem.MovieItem
import tv.trakt.trakt.core.discover.model.DiscoverItem.ShowItem
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AllDiscoverListView(
    state: LazyListState,
    collectionState: UserCollectionState,
    filter: MediaMode,
    items: ImmutableList<DiscoverItem>,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    loading: Boolean,
    loadingMore: Boolean,
    onItemClick: (DiscoverItem) -> Unit = {},
    onItemLongClick: (DiscoverItem) -> Unit = {},
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
            loading = loading,
            collectionState = collectionState,
            mediaIcon = (filter == MediaMode.MEDIA),
            onClick = onItemClick,
            onLongClick = onItemLongClick,
        )

        if (loading && items.isEmpty()) {
            items(5) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (loadingMore) {
            item {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (items.isEmpty() && !loading) {
            item {
                ContentEmptyView(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }
    }
}

private fun LazyListScope.listItems(
    items: ImmutableList<DiscoverItem>,
    collectionState: UserCollectionState,
    mediaIcon: Boolean,
    loading: Boolean,
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
                enabled = !loading,
                watched = collectionState.isWatched(item.id, item.type, item.airedEpisodes),
                watchlist = collectionState.isWatchlist(item.id, item.type),
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
                enabled = !loading,
                watched = collectionState.isWatched(item.id, item.type, item.airedEpisodes),
                watchlist = collectionState.isWatchlist(item.id, item.type),
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
    enabled: Boolean,
    watched: Boolean,
    watchlist: Boolean,
    modifier: Modifier = Modifier,
    mediaIcon: Boolean,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val genresText = item.show.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    PanelMediaCard(
        enabled = enabled,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(THUMB),
        watched = watched,
        watchlist = watchlist,
        onClick = onClick,
        onImageClick = onClick,
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
    enabled: Boolean,
    modifier: Modifier = Modifier,
    mediaIcon: Boolean,
    watched: Boolean,
    watchlist: Boolean,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val genresText = item.movie.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    PanelMediaCard(
        enabled = enabled,
        title = item.movie.title,
        titleOriginal = item.movie.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(THUMB),
        watched = watched,
        watchlist = watchlist,
        onClick = onClick,
        onImageClick = onClick,
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
private fun AllDiscoverListViewPreview() {
    TraktTheme {
        AllDiscoverListView(
            state = LazyListState(),
            collectionState = UserCollectionState.Default,
            filter = MediaMode.MEDIA,
            loading = false,
            loadingMore = false,
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
