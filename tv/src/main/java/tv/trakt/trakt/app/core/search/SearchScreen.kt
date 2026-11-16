package tv.trakt.trakt.app.core.search

import TraktTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.mediacards.CustomListSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.people.navigation.PersonDestination
import tv.trakt.trakt.app.core.search.views.SearchFiltersList
import tv.trakt.trakt.app.core.search.views.SearchGridItem
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.search.SearchState
import tv.trakt.trakt.common.core.search.SearchViewModel
import tv.trakt.trakt.common.core.search.model.SearchFilter.LISTS
import tv.trakt.trakt.common.core.search.model.SearchFilter.MEDIA
import tv.trakt.trakt.common.core.search.model.SearchFilter.MOVIES
import tv.trakt.trakt.common.core.search.model.SearchFilter.PEOPLE
import tv.trakt.trakt.common.core.search.model.SearchFilter.SHOWS
import tv.trakt.trakt.common.core.search.model.SearchInput
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.resources.R

private const val SKELETON_ROWS = 2
private const val LIST_COLUMNS = 3

@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel,
    backgroundUrl: String?,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onPersonClick: (PersonDestination) -> Unit,
    onListClick: (CustomList) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigatePerson,
        state.navigateList,
    ) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it.ids.trakt)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it.ids.trakt)
        }
        state.navigatePerson?.let {
            viewModel.clearNavigation()
            onPersonClick(
                PersonDestination(
                    personId = it.ids.trakt.value,
                    sourceId = 0,
                    backdropUrl = it.images?.getFanartUrl(Size.FULL),
                ),
            )
        }
        state.navigateList?.let {
            viewModel.clearNavigation()
            onListClick(it)
        }
    }

    SearchScreenContent(
        state = state,
        backgroundUrl = backgroundUrl,
        onSearchInput = viewModel::updateSearch,
        onShowClick = viewModel::navigateToShow,
        onMovieClick = viewModel::navigateToMovie,
        onPersonClick = viewModel::navigateToPerson,
        onListClick = viewModel::navigateToList,
    )
}

@Composable
private fun SearchScreenContent(
    state: SearchState,
    backgroundUrl: String?,
    modifier: Modifier = Modifier,
    onSearchInput: (SearchInput) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onPersonClick: (Person) -> Unit = {},
    onListClick: (CustomList) -> Unit = {},
) {
    val searchInputState = rememberTextFieldState(state.input.query)

    var focusedImageUrl by remember { mutableStateOf<String?>(null) }
    var focusedItemKey by rememberSaveable { mutableStateOf<String?>(null) }

    val inputFocusRequester = remember { FocusRequester() }
    val filtersFocusRequester = remember { FocusRequester() }
    val itemFocusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(Unit) {
        val itemFocusRequester = focusedItemKey?.let { itemFocusRequesters[it] }
        (itemFocusRequester ?: inputFocusRequester).requestSafeFocus()
    }

    LaunchedEffect(searchInputState.text) {
        onSearchInput(
            state.input.copy(query = searchInputState.text.toString()),
        )
    }

    val isSearching = state.input.query.isNotBlank()
    val isLists = state.input.filter == LISTS

    val items = when {
        isSearching -> state.searchResult?.items
        else -> state.popularResults?.items
    } ?: EmptyImmutableList

    val loading = when {
        isSearching -> state.searching
        else -> state.popularLoading.isLoading
    }

    val listState = rememberLazyListState()
    val firstItemFocusRequester = {
        val firstKey = items.firstOrNull()?.key
        val isFirstVisible = listState.layoutInfo.visibleItemsInfo.any { it.key == firstKey }
        when {
            isFirstVisible -> itemFocusRequesters[firstKey] ?: FocusRequester.Default
            else -> FocusRequester.Default
        }
    }

    val gridSpace = TraktTheme.spacing.mainGridSpace
    val startSpace = TraktTheme.spacing.mainContentStartSpace
    val endSpace = TraktTheme.spacing.mainContentEndSpace
    val rowPadding = PaddingValues(start = startSpace, end = endSpace)
    val mediaCardSize = TraktTheme.size.verticalMediaCardSize

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        if (!isLists) {
            BackdropImage(
                imageUrl = focusedImageUrl ?: backgroundUrl,
                saturation = 0F,
                crossfade = true,
            )
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val columns = when {
                isLists -> {
                    LIST_COLUMNS
                }
                else -> {
                    val rowWidth = maxWidth - startSpace - endSpace
                    ((rowWidth + gridSpace) / (mediaCardSize + gridSpace)).toInt().coerceAtLeast(1)
                }
            }

            val rows = remember(items, columns) {
                items.chunked(columns)
            }

            LazyColumn(
                state = listState,
                verticalArrangement = spacedBy(gridSpace * 1.5F),
                contentPadding = PaddingValues(
                    top = TraktTheme.spacing.mainContentVerticalSpace,
                    bottom = TraktTheme.spacing.mainContentVerticalSpace,
                ),
                overscrollEffect = null,
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "input") {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusGroup(),
                    ) {
                        TraktTextField(
                            state = searchInputState,
                            placeholder = stringResource(state.input.filter.placeholderRes),
                            icon = painterResource(R.drawable.ic_search_off),
                            loading = state.searching,
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .width(400.dp)
                                .focusRequester(inputFocusRequester)
                                .focusProperties { down = filtersFocusRequester }
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        focusedItemKey = null
                                        focusedImageUrl = null
                                    }
                                },
                        )
                    }
                }

                item(key = "filters") {
                    SearchFiltersList(
                        selectedFilter = state.input.filter,
                        firstFocusRequester = filtersFocusRequester,
                        downFocusRequester = firstItemFocusRequester,
                        onFilterClick = {
                            onSearchInput(
                                SearchInput(
                                    query = searchInputState.text.toString(),
                                    filter = it,
                                ),
                            )
                        },
                    )
                }

                item(key = "header") {
                    Text(
                        text = when {
                            isSearching -> stringResource(state.input.filter.displayRes)
                            else -> when (state.input.filter) {
                                MEDIA, SHOWS, MOVIES -> stringResource(R.string.list_title_most_popular_searches)
                                PEOPLE -> stringResource(R.string.list_title_birthdays_this_month)
                                LISTS -> stringResource(R.string.list_title_popular_lists)
                            }
                        },
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading5,
                        modifier = Modifier.padding(rowPadding),
                    )
                }

                val error = state.error
                when {
                    error != null -> errorItem(error, rowPadding)
                    items.isEmpty() && loading -> skeletonRows(columns, isLists, rowPadding)
                    isSearching && state.searchResult?.items?.isEmpty() == true -> emptyItem(
                        query = state.input.query,
                        rowPadding = rowPadding,
                    )
                    else -> items(
                        items = rows,
                        key = { rowItems -> rowItems.first().key },
                    ) { rowItems ->
                        GridRow(
                            rowItems = rowItems,
                            columns = columns,
                            modifier = Modifier.padding(rowPadding),
                        ) { item ->
                            val focusRequester = remember(item.key) {
                                itemFocusRequesters.getOrPut(item.key) { FocusRequester() }
                            }

                            SearchGridItem(
                                item = item,
                                collection = state.collection,
                                onShowClick = onShowClick,
                                onMovieClick = onMovieClick,
                                onPersonClick = onPersonClick,
                                onListClick = onListClick,
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            focusedItemKey = item.key
                                            focusedImageUrl = item.images?.getFanartUrl(Size.FULL)
                                        }
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> GridRow(
    rowItems: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit,
) {
    Row(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridSpace),
        modifier = modifier
            .fillMaxWidth()
            .focusGroup(),
    ) {
        rowItems.forEach { item ->
            Box(
                propagateMinConstraints = true,
                modifier = Modifier.weight(1F),
            ) {
                itemContent(item)
            }
        }
        repeat(columns - rowItems.size) {
            Box(modifier = Modifier.weight(1F))
        }
    }
}

private fun LazyListScope.errorItem(
    error: Exception,
    rowPadding: PaddingValues,
) {
    item(key = "error") {
        Text(
            text = "${stringResource(R.string.error_text_unexpected_error_short)}\n\n$error",
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
            maxLines = 10,
            modifier = Modifier.padding(rowPadding),
        )
    }
}

private fun LazyListScope.emptyItem(
    query: String,
    rowPadding: PaddingValues,
) {
    item(key = "empty") {
        Text(
            text = stringResource(R.string.spotlight_no_results, query),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmall,
            modifier = Modifier.padding(rowPadding),
        )
    }
}

private fun LazyListScope.skeletonRows(
    columns: Int,
    isLists: Boolean,
    rowPadding: PaddingValues,
) {
    items(count = SKELETON_ROWS) {
        GridRow(
            rowItems = List(columns) { it },
            columns = columns,
            modifier = Modifier.padding(rowPadding),
        ) {
            when {
                isLists -> CustomListSkeletonCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TraktTheme.size.detailsCustomListSize),
                )
                else -> VerticalMediaSkeletonCard()
            }
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        SearchScreenContent(
            state = SearchState(),
            backgroundUrl = null,
        )
    }
}
