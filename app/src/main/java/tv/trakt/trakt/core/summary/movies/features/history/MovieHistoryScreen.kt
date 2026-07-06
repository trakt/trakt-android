@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.movies.features.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.isTraktUnknown
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.sections.activity.features.all.views.AllActivityMovieItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.time.ZoneId

@Composable
internal fun MovieHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: MovieHistoryViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveSheet by remember { mutableStateOf<MovieItem?>(null) }

    MovieHistoryContent(
        state = state,
        modifier = modifier,
        backgroundUrl = state.media.background,
        onLoadMore = viewModel::loadMoreData,
        onBackClick = onNavigateBack,
        onRemoveClick = {
            confirmRemoveSheet = it
        },
    )

    RemoveConfirmationSheet(
        active = confirmRemoveSheet != null,
        onYes = {
            confirmRemoveSheet?.let {
                viewModel.removeFromWatched(it)
                confirmRemoveSheet = null
            }
        },
        onNo = {
            confirmRemoveSheet = null
        },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_single_watched,
            state.media.title,
        ),
    )
}

@Composable
private fun TitleBar(
    subtitle: String,
    loading: Boolean,
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
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
        )

        TraktHeader(
            title = stringResource(R.string.list_title_history),
            subtitle = subtitle,
        )

        if (loading) {
            Spacer(modifier = Modifier.weight(1f))
            FilmProgressIndicator(
                size = 16.dp,
            )
        }
    }
}

@Composable
internal fun MovieHistoryContent(
    state: MovieHistoryState,
    modifier: Modifier = Modifier,
    backgroundUrl: String? = null,
    onLoadMore: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onRemoveClick: (MovieItem) -> Unit = {},
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
            imageUrl = backgroundUrl,
            translation = listScrollConnection.resultOffset,
        )

        ContentList(
            listTitle = state.media.title,
            listState = listState,
            listItems = remember(state.items) {
                (state.items ?: emptyMap()).toImmutableMap()
            },
            listRatings = remember(state.itemsRatings) {
                (state.itemsRatings ?: emptyMap()).toImmutableMap()
            },
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            watchedCount = state.media.watched,
            onEndOfList = onLoadMore,
            onBackClick = onBackClick,
            onRemoveClick = onRemoveClick,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listTitle: String,
    listItems: ImmutableMap<LocalDate, ImmutableList<MovieItem>>,
    listRatings: ImmutableMap<String, UserRating>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    loading: Boolean,
    watchedCount: Int,
    onEndOfList: () -> Unit,
    onBackClick: () -> Unit,
    onRemoveClick: (MovieItem) -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 5
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
                subtitle = listTitle,
                loading = loading && listItems.isNotEmpty(),
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .onClick {
                        onBackClick()
                    },
            )
        }

        val today = nowLocalDay()
        listItems.keys.forEachIndexed { index, date ->
            val itemsForDate = listItems[date] ?: EmptyImmutableList

            item(key = "header-$date") {
                TraktHeader(
                    title = when {
                        today == date || today.minusDays(1) == date -> {
                            date.atStartOfDay(ZoneId.systemDefault()).relativePastDateString()
                        }
                        date.isTraktUnknown() -> {
                            stringResource(R.string.button_text_mark_as_watched_unknown_date)
                        }
                        else -> {
                            date.format(longDateFormat()).capitalize()
                        }
                    },
                    modifier = Modifier
                        .padding(
                            top = if (index == 0) 8.dp else 16.dp,
                            bottom = 12.dp,
                        ),
                )
            }

            items(
                items = itemsForDate,
                key = { it.id },
            ) { item ->
                ContentListItem(
                    item = item,
                    itemRating = listRatings[item.key],
                    enabled = !loading,
                    onRemoveClick = onRemoveClick,
                )
            }
        }

        if (loading && listItems.isEmpty()) {
            items(watchedCount.coerceAtLeast(1)) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace),
                )
            }
        }

        if (!loading && listItems.isEmpty()) {
            item {
                ContentEmptyView(
                    modifier = Modifier
                        .animateItem(),
                )
            }
        }
    }
}

@Composable
private fun LazyItemScope.ContentListItem(
    item: MovieItem,
    itemRating: UserRating?,
    enabled: Boolean,
    onRemoveClick: (MovieItem) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace),
    ) {
        AllActivityMovieItem(
            item = item,
            itemRating = itemRating,
            moreButton = true,
            enabled = enabled,
            dateFormat = longDateTimeFormat(),
            onClick = {},
            onLongClick = { showMenu = true },
        )

        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            DropdownMenu(
                expanded = showMenu,
                containerColor = TraktTheme.colors.dialogContainer,
                shape = RoundedCornerShape(16.dp),
                onDismissRequest = {
                    showMenu = false
                },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.button_text_remove_from_history),
                            style = TraktTheme.typography.buttonTertiary,
                            color = TraktTheme.colors.textPrimary,
                        )
                    },
                    onClick = {
                        onRemoveClick(item)
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_trash),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                )
            }
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

// -- Previews --

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieHistoryContent(
                state = MovieHistoryState(
                    loading = Loading,
                    media = MovieHistoryState.Media(
                        id = 1.toTraktId(),
                        title = "Test",
                        background = null,
                        watched = 2,
                    ),
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieHistoryContent(
                state = MovieHistoryState(
                    loading = LoadingState.Done,
                    media = MovieHistoryState.Media(
                        id = 1.toTraktId(),
                        title = "Test",
                        background = null,
                        watched = 3,
                    ),
                ),
            )
        }
    }
}
