@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.sections.smart.details

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsMovieView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsShowView
import tv.trakt.trakt.core.lists.model.SmartListItem
import tv.trakt.trakt.core.lists.model.SmartListItem.MovieItem
import tv.trakt.trakt.core.lists.model.SmartListItem.ShowItem
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.list.SmartListDropdown
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SmartListDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: SmartListDetailsViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snack = LocalSnackbarState.current

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snack.showSnackbar(
                message = state.error?.localizedMessage ?: "",
                duration = SnackbarDuration.Long,
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.navigateShow, state.navigateMovie) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
    }

    var showContextSheet by remember { mutableStateOf<ShowItem?>(null) }
    var movieContextSheet by remember { mutableStateOf<MovieItem?>(null) }
    var confirmDeleteSheet by remember { mutableStateOf(false) }

    SmartListDetailsContent(
        state = state,
        modifier = modifier,
        onLoadMoreData = viewModel::loadMoreData,
        onClick = {
            when (it) {
                is MovieItem -> viewModel.navigateToMovie(it.movie)
                is ShowItem -> viewModel.navigateToShow(it.show)
            }
        },
        onLongClick = {
            when (it) {
                is MovieItem -> movieContextSheet = it
                is ShowItem -> showContextSheet = it
            }
        },
        onDeleteClick = {
            confirmDeleteSheet = true
        },
        onBackClick = onNavigateBack,
    )

    ShowContextSheet(
        show = showContextSheet?.show,
        onDismiss = {
            showContextSheet = null
        },
    )

    MovieContextSheet(
        movie = movieContextSheet?.movie,
        onDismiss = {
            movieContextSheet = null
        },
    )

    RemoveConfirmationSheet(
        active = confirmDeleteSheet,
        title = stringResource(R.string.confirmation_title_delete_list),
        message = stringResource(
            R.string.warning_prompt_delete_list,
            state.list?.name.orEmpty(),
        ),
        onYes = {
            viewModel.deleteList()
            confirmDeleteSheet = false
        },
        onNo = { confirmDeleteSheet = false },
    )
}

@Composable
internal fun SmartListDetailsContent(
    state: SmartListDetailsState,
    modifier: Modifier = Modifier,
    onClick: (SmartListItem) -> Unit = {},
    onLongClick: (SmartListItem) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMoreData: () -> Unit = {},
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
            title = state.list?.name ?: "",
            subtitle = state.list?.rememberDescription(),
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            collectionState = state.collection,
            contentPadding = contentPadding,
            loading = state.loading.isLoading || state.deleting.isLoading,
            loadingMore = state.loadingMore.isLoading,
            onEndOfList = onLoadMoreData,
            onClick = onClick,
            onLongClick = onLongClick,
            onDeleteClick = onDeleteClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun TitleBar(
    title: String,
    subtitle: String?,
    subtitleVisible: Boolean,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F, fill = false)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                }
                .onClick {
                    onBackClick()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )

            TraktHeader(
                title = title,
                subtitle = when {
                    subtitleVisible -> subtitle
                    else -> null
                },
                modifier = Modifier
                    .weight(1F, fill = false),
            )
        }

        SmartListDropdown(
            onDeleteClick = onDeleteClick,
            modifier = Modifier
                .padding(start = 12.dp),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    title: String,
    subtitle: String?,
    listState: LazyListState,
    listItems: ImmutableList<SmartListItem>,
    collectionState: UserCollectionState,
    loading: Boolean,
    loadingMore: Boolean,
    onClick: (SmartListItem) -> Unit,
    onLongClick: (SmartListItem) -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    onEndOfList: () -> Unit,
) {
    var subtitleCollapsed by remember { mutableStateOf(true) }

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
                title = title,
                subtitle = subtitle,
                subtitleVisible = false,
                onDeleteClick = onDeleteClick,
                onBackClick = onBackClick,
            )
        }

        item {
            Text(
                text = subtitle ?: "",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta.copy(
                    fontWeight = W400,
                    lineHeight = 1.1.em,
                ),
                maxLines = when {
                    subtitleCollapsed -> 3
                    else -> Int.MAX_VALUE
                },
                overflow = Ellipsis,
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .onClick {
                        subtitleCollapsed = !subtitleCollapsed
                    },
            )
        }

        itemsIndexed(
            items = listItems,
            key = { _, item -> item.key },
        ) { itemIndex, item ->
            val shadow = itemIndex == 0
            when (item) {
                is ShowItem -> ListDetailsShowView(
                    show = item.show,
                    showUserRating = null,
                    shadow = shadow,
                    enabled = !loading,
                    watched = collectionState.isWatched(item.id, item.type, item.show.airedEpisodes),
                    watching = collectionState.isWatching(item.id, item.type, item.show.airedEpisodes),
                    watchlist = collectionState.isWatchlist(item.id, item.type),
                    showIcon = true,
                    sorting = Sorting.Default,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )

                is MovieItem -> ListDetailsMovieView(
                    movie = item.movie,
                    movieUserRating = null,
                    shadow = shadow,
                    enabled = !loading,
                    watched = collectionState.isWatched(item.id, item.type, null),
                    watchlist = collectionState.isWatchlist(item.id, item.type),
                    showIcon = true,
                    sorting = Sorting.Default,
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

        if (loading && listItems.isEmpty()) {
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
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "en",
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            SmartListDetailsContent(
                state = SmartListDetailsState(
                    loading = LoadingState.Loading,
                ),
            )
        }
    }
}
