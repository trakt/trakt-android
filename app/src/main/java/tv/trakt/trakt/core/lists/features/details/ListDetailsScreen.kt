@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.features.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.helpers.extensions.toAnnotatedString
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.SortTypeList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.features.details.ListDetailsState.LikedInfo
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsEpisodeView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsMovieView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsSeasonView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsShowView
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.CustomListItem.EpisodeItem
import tv.trakt.trakt.core.lists.model.CustomListItem.MovieItem
import tv.trakt.trakt.core.lists.model.CustomListItem.SeasonItem
import tv.trakt.trakt.core.lists.model.CustomListItem.ShowItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.components.sorting.SortingSplitButton
import tv.trakt.trakt.ui.components.sorting.sheets.SortSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

private const val LIST_DESCRIPTION_LIMIT = 40

@Composable
internal fun ListDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onEpisodeClick: (TraktId, Episode) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snack = LocalSnackbarState.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state.info, state.error) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            snack.showSnackbar(
                message = state.info?.get(context) ?: "",
                duration = SnackbarDuration.Short,
            )
            viewModel.clearInfoError()
        }

        if (state.error != null) {
            snack.showSnackbar(
                message = state.error?.localizedMessage ?: "",
                duration = SnackbarDuration.Long,
            )
            viewModel.clearInfoError()
        }
    }

    LaunchedEffect(
        state.navigateMovie,
        state.navigateShow,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let { (showId, episode) ->
            onEpisodeClick(showId, episode)
            viewModel.clearNavigation()
        }
    }

    var showContextSheet by remember { mutableStateOf<ShowItem?>(null) }
    var movieContextSheet by remember { mutableStateOf<MovieItem?>(null) }
    var sortSheet by remember { mutableStateOf<SortTypeList?>(null) }
    var confirmRemoveLikeSheet by remember { mutableStateOf(false) }

    ListDetailsContent(
        state = state,
        modifier = modifier,
        onLoadMoreData = viewModel::loadMoreData,
        onClick = {
            if (it.id == state.list?.mediaId) {
                onNavigateBack()
                return@ListDetailsContent
            }

            when (it) {
                is MovieItem -> viewModel.navigateToMovie(it.movie)
                is ShowItem -> viewModel.navigateToShow(it.show)
                is SeasonItem -> viewModel.navigateToShow(it.show)
                is EpisodeItem -> viewModel.navigateToEpisode(it.show, it.episode)
            }
        },
        onLongClick = {
            when (it) {
                is MovieItem -> movieContextSheet = it
                is ShowItem -> showContextSheet = it
                is SeasonItem, is EpisodeItem -> Unit
            }
        },
        onFilterClick = viewModel::setFilter,
        onSortTypeClick = {
            if (!state.loading.isLoading) {
                sortSheet = state.sorting.type
            }
        },
        onSortOrderClick = {
            if (!state.loading.isLoading) {
                val sorting = state.sorting
                viewModel.setSorting(
                    sorting.copy(
                        order = sorting.order.toggle(),
                    ),
                )
            }
        },
        onLikeClick = {
            state.liked
                ?.takeIf { !it.loading }
                ?.let {
                    when {
                        it.liked -> confirmRemoveLikeSheet = true
                        else -> viewModel.setLiked(true)
                    }
                }
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

    SortSelectionSheet(
        active = sortSheet != null,
        selected = sortSheet,
        onResult = {
            viewModel.setSorting(
                state.sorting.copy(
                    type = it,
                ),
            )
        },
        onDismiss = {
            sortSheet = null
        },
    )

    RemoveConfirmationSheet(
        active = confirmRemoveLikeSheet,
        onYes = {
            viewModel.setLiked(false)
            confirmRemoveLikeSheet = false
        },
        onNo = { confirmRemoveLikeSheet = false },
        title = stringResource(R.string.list_title_liked_lists),
        message = stringResource(
            R.string.warning_prompt_remove_from_liked_lists,
            state.list?.list?.name ?: "",
        ),
    )
}

@Composable
internal fun ListDetailsContent(
    state: ListDetailsState,
    modifier: Modifier = Modifier,
    onClick: (CustomListItem) -> Unit = {},
    onLongClick: (CustomListItem) -> Unit = {},
    onFilterClick: (MediaMode) -> Unit = {},
    onSortTypeClick: () -> Unit = {},
    onSortOrderClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
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

        val userId = state.user?.ids?.trakt
        val listUserId = state.list?.list?.user?.ids?.trakt

        ContentList(
            title = state.list?.list?.name ?: "",
            subtitle = state.list?.list?.description?.trim(),
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            listFilter = state.filter,
            listSorting = state.sorting,
            listLiked = when {
                userId == listUserId -> null
                else -> state.liked
            },
            listLikes = state.list?.list?.likes,
            collectionState = state.collection,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            loadingMore = state.loadingMore.isLoading,
            onEndOfList = onLoadMoreData,
            onClick = onClick,
            onLongClick = onLongClick,
            onFilterClick = onFilterClick,
            onSortTypeClick = onSortTypeClick,
            onSortOrderClick = onSortOrderClick,
            onLikeClick = onLikeClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun TitleBar(
    title: String,
    subtitle: String?,
    subtitleVisible: Boolean,
    liked: LikedInfo?,
    likesCount: Int?,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit,
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

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TraktHeader(
                    title = title,
                    subtitle = when {
                        subtitleVisible -> subtitle
                        else -> null
                    },
                    modifier = Modifier
                        .weight(1F, fill = false),
                )

                AnimatedVisibility(
                    visible = liked != null,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(150)),
                    modifier = Modifier.padding(start = 32.dp),
                ) {
                    Row(
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = spacedBy(6.dp),
                        modifier = Modifier
                            .alpha(if (liked?.loading == true) 0.25F else 1F)
                            .onClick(
                                enabled = liked?.loading != true,
                                onClick = onLikeClick,
                            ),
                    ) {
                        Icon(
                            painter = painterResource(
                                when {
                                    liked?.liked ?: false -> R.drawable.ic_thumb_up2_fill
                                    else -> R.drawable.ic_thumb_up2
                                },
                            ),
                            tint = TraktTheme.colors.textPrimary,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    translationY = -0.75.dp.toPx()
                                },
                        )
                        likesCount?.let {
                            Text(
                                text = rememberThousandsFormat(it),
                                style = TraktTheme.typography.buttonTertiary.copy(
                                    fontSize = 12.sp,
                                ),
                                color = TraktTheme.colors.textPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    title: String,
    subtitle: String?,
    listState: LazyListState,
    listItems: ImmutableList<CustomListItem>,
    listFilter: MediaMode?,
    listSorting: Sorting?,
    listLiked: LikedInfo?,
    listLikes: Int?,
    collectionState: UserCollectionState,
    loading: Boolean,
    loadingMore: Boolean,
    onClick: (CustomListItem) -> Unit,
    onLongClick: (CustomListItem) -> Unit,
    onFilterClick: (MediaMode) -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onLikeClick: () -> Unit,
    onBackClick: () -> Unit,
    onEndOfList: () -> Unit,
) {
    val subtitleVisible = remember(subtitle) {
        (subtitle?.length ?: 0) <= LIST_DESCRIPTION_LIMIT
    }

    var subtitleCollapsed by remember { mutableStateOf(true) }

    val subtitleHtmlText = remember(subtitle) {
        HtmlCompat
            .fromHtml(subtitle ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toAnnotatedString()
    }

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
                subtitleVisible = subtitleVisible,
                liked = listLiked,
                likesCount = listLikes,
                onLikeClick = onLikeClick,
                onBackClick = onBackClick,
            )
        }

        if (!subtitleVisible) {
            item {
                Text(
                    text = subtitleHtmlText,
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
                        .padding(bottom = 10.dp)
                        .onClick {
                            subtitleCollapsed = !subtitleCollapsed
                        },
                )
            }
        }

        if (listSorting != null) {
            item {
                ContentFilters(
                    subtitle = !subtitle.isNullOrEmpty(),
                    filter = listFilter,
                    sorting = listSorting,
                    onFilterClick = onFilterClick,
                    onSortTypeClick = onSortTypeClick,
                    onSortOrderClick = onSortOrderClick,
                )
            }
        }

        if (listItems.isNotEmpty()) {
            itemsIndexed(
                items = listItems,
                key = { _, item -> item.key },
            ) { index, item ->
                when (item) {
                    is ShowItem -> ListDetailsShowView(
                        item = item,
                        shadow = index == 0,
                        enabled = !loading,
                        watched = collectionState.isWatched(item.id, item.type, item.show.airedEpisodes),
                        watchlist = collectionState.isWatchlist(item.id, item.type),
                        showIcon = true,
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
                        item = item,
                        shadow = index == 0,
                        enabled = !loading,
                        watched = collectionState.isWatched(item.id, item.type, null),
                        watchlist = collectionState.isWatchlist(item.id, item.type),
                        showIcon = true,
                        onClick = { onClick(item) },
                        onLongClick = { onLongClick(item) },
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )

                    is SeasonItem -> ListDetailsSeasonView(
                        item = item,
                        shadow = index == 0,
                        enabled = !loading,
                        onClick = { onClick(item) },
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )

                    is EpisodeItem -> ListDetailsEpisodeView(
                        item = item,
                        shadow = index == 0,
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
                ContentEmpty(filter = listFilter)
            }
        }
    }
}

@Composable
private fun ContentFilters(
    subtitle: Boolean,
    filter: MediaMode?,
    sorting: Sorting,
    onFilterClick: (MediaMode) -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = if (subtitle) 8.dp else 0.dp,
                bottom = 19.dp,
            ),
    ) {
        if (filter != null) {
            MediaModeFilters(
                selected = filter,
                onClick = onFilterClick,
                height = 32.dp,
                unselectedTextVisible = false,
            )
        }

        SortingSplitButton(
            text = stringResource(sorting.type.displayStringRes),
            order = sorting.order,
            height = 32.dp,
            onLeadingClick = onSortTypeClick,
            onTrailingClick = onSortOrderClick,
        )
    }
}

@Composable
private fun ContentEmpty(filter: MediaMode?) {
    Text(
        text = stringResource(
            when (filter) {
                MediaMode.MOVIES -> R.string.list_placeholder_personal_list_empty_movies
                MediaMode.SHOWS -> R.string.list_placeholder_personal_list_empty_shows
                else -> R.string.list_placeholder_empty
            },
        ),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
    )
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
            ListDetailsContent(
                state = ListDetailsState(
                    loading = LoadingState.Loading,
                    liked = LikedInfo(
                        liked = false,
                        loading = false,
                    ),
                    list = ListDetailsState.ListDetails(
                        list = PreviewData.customList1,
                        mediaId = TraktId(1),
                    ),
                ),
            )
        }
    }
}
