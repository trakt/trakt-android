@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.sections.personal.features.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.model.PersonalListItem.MovieItem
import tv.trakt.trakt.core.lists.model.PersonalListItem.ShowItem
import tv.trakt.trakt.core.lists.sections.personal.features.all.views.AllPersonalListMovieView
import tv.trakt.trakt.core.lists.sections.personal.features.all.views.AllPersonalListShowView
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.sheet.ListMovieContextSheet
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.sheet.ListShowContextSheet
import tv.trakt.trakt.core.lists.sheets.EditListSheet
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllPersonalListScreen(
    modifier: Modifier = Modifier,
    viewModel: AllPersonalListViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateMovie, state.navigateShow) {
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
    var editListSheet by remember { mutableStateOf<CustomList?>(null) }

    AllPersonalListContent(
        state = state,
        modifier = modifier,
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
        onFilterClick = viewModel::setFilter,
        onMoreClick = {
            editListSheet = state.list
        },
        onBackClick = onNavigateBack,
    )

    ListShowContextSheet(
        show = showContextSheet?.show,
        list = state.list,
        onRemoveListItem = {
            viewModel.removeItem(showContextSheet)
        },
        onDismiss = {
            showContextSheet = null
        },
    )

    ListMovieContextSheet(
        movie = movieContextSheet?.movie,
        list = state.list,
        onRemoveListItem = {
            viewModel.removeItem(movieContextSheet)
        },
        onDismiss = {
            movieContextSheet = null
        },
    )

    EditListSheet(
        active = editListSheet != null,
        list = editListSheet,
        onListEdited = viewModel::loadDetails,
        onListDeleted = onNavigateBack,
        onDismiss = { editListSheet = null },
    )
}

@Composable
internal fun AllPersonalListContent(
    state: AllPersonalListState,
    modifier: Modifier = Modifier,
    onTopOfList: () -> Unit = {},
    onClick: (PersonalListItem) -> Unit = {},
    onLongClick: (PersonalListItem) -> Unit = {},
    onFilterClick: (MediaMode) -> Unit = {},
    onBackClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
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
            title = state.list?.name ?: "",
            subtitle = state.list?.description,
            listState = listState,
            listFilter = state.filter,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            contentPadding = contentPadding,
            onClick = onClick,
            onLongClick = onLongClick,
            onFilterClick = onFilterClick,
            onTopOfList = onTopOfList,
            onBackClick = onBackClick,
            onMoreClick = onMoreClick,
        )
    }
}

@Composable
private fun TitleBar(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
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
                subtitle = subtitle,
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_edit),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxHeight()
                .onClick { onMoreClick() }
                .size(18.dp),
        )
    }
}

@Composable
private fun ContentFilters(
    hasSubtitle: Boolean,
    watchlistFilter: MediaMode,
    onFilterClick: (MediaMode) -> Unit,
) {
    MediaModeFilters(
        selected = watchlistFilter,
        onClick = onFilterClick,
        paddingVertical = PaddingValues(
            top = if (hasSubtitle) 8.dp else 0.dp,
            bottom = 19.dp,
        ),
    )
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    listState: LazyListState,
    listItems: ImmutableList<PersonalListItem>,
    listFilter: MediaMode?,
    contentPadding: PaddingValues,
    onClick: (PersonalListItem) -> Unit,
    onLongClick: (PersonalListItem) -> Unit,
    onFilterClick: (MediaMode) -> Unit,
    onTopOfList: () -> Unit,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
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
                onBackClick = onBackClick,
                onMoreClick = onMoreClick,
            )
        }

        if (listFilter != null) {
            item {
                ContentFilters(
                    hasSubtitle = !subtitle.isNullOrEmpty(),
                    watchlistFilter = listFilter,
                    onFilterClick = onFilterClick,
                )
            }
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            when (item) {
                is ShowItem -> AllPersonalListShowView(
                    item = item,
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
                is MovieItem -> AllPersonalListMovieView(
                    item = item,
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
            }
        }

        if (listItems.isEmpty()) {
            item {
                ContentEmpty()
            }
        }
    }
}

@Composable
private fun ContentEmpty() {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
    )
}
