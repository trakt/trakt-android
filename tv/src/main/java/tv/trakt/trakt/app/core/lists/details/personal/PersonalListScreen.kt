package tv.trakt.trakt.app.core.lists.details.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.app.core.lists.filters.TvListControls
import tv.trakt.trakt.app.core.lists.filters.TvListControlsState
import tv.trakt.trakt.app.core.lists.filters.TvListEmptyState
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun PersonalListScreen(
    viewModel: PersonalListViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PersonalListContent(
        state = state,
        listName = viewModel.destination.listName,
        onShowClick = onNavigateToShow,
        onMovieClick = onNavigateToMovie,
        onLoadNextPage = { viewModel.loadNextDataPage() },
        onFilterApplied = viewModel::applyFilter,
        onSortingApplied = viewModel::applySorting,
    )
}

@Composable
private fun PersonalListContent(
    state: PersonalListState,
    listName: String,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onLoadNextPage: () -> Unit,
    onFilterApplied: (GlobalFilter) -> Unit,
    onSortingApplied: (Sorting) -> Unit,
) {
    var focusedItem by remember { mutableStateOf<PersonalListItem?>(null) }
    var focusedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(Unit) {
        delay(500.milliseconds)
        focusRequesters[focusedItemId]?.requestSafeFocus()
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedItemId]?.requestSafeFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedItem?.images?.getFanartUrl(Images.Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = TraktTheme.size.verticalMediaCardSize),
            horizontalArrangement = Arrangement.spacedBy(gridSpace),
            verticalArrangement = Arrangement.spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = listName,
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1F)
                            .focusProperties {
                                down = focusRequesters.values.firstOrNull() ?: FocusRequester.Default
                            }
                            .focusable(),
                    )

                    TvListControls(
                        state = TvListControlsState(
                            filter = state.filter,
                            sorting = state.sorting,
                            configuration = TvListFilterConfiguration.MixedList,
                        ),
                        onFilterApplied = onFilterApplied,
                        onSortingApplied = onSortingApplied,
                    )
                }
            }

            if (state.isLoading && state.items.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.items.isNullOrEmpty()) {
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].id },
                ) { index ->
                    val item = state.items[index]

                    val focusRequester = remember(item.id) {
                        focusRequesters.getOrPut(item.id) {
                            FocusRequester()
                        }
                    }

                    item.show?.let { show ->
                        VerticalMediaCard(
                            title = show.title,
                            imageUrl = show.images?.getPosterUrl(),
                            watched = state.collection.isWatched(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                            watching = state.collection.isWatching(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                            watchlist = state.collection.isWatchlist(show.ids.trakt, MediaType.Show),
                            onClick = {
                                if (!state.isLoadingPage) {
                                    onShowClick(show.ids.trakt)
                                }
                            },
                            chipContent = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(1.dp),
                                ) {
                                    val episodes = show.airedEpisodes.takeIf { it > 0 }
                                        ?.let { stringResource(R.string.tag_text_number_of_episodes, it) }
                                    val text = listOfNotNull(show.year?.toString(), episodes)
                                        .joinToString("  •  ")

                                    if (text.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_shows_off),
                                                contentDescription = null,
                                                tint = TraktTheme.colors.textPrimary,
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .graphicsLayer {
                                                        translationY = -0.5.dp.toPx()
                                                    },
                                            )

                                            Text(
                                                text = text,
                                                style = TraktTheme.typography.cardTitle,
                                                color = TraktTheme.colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        focusedItem = item
                                        focusedItemId = item.id

                                        loadNextPageIfNeeded(
                                            size = state.items.size,
                                            index = index,
                                            onLoadNextPage = onLoadNextPage,
                                        )
                                    }
                                },
                        )
                    }

                    item.movie?.let { movie ->
                        VerticalMediaCard(
                            title = movie.title,
                            imageUrl = movie.images?.getPosterUrl(),
                            watched = state.collection.isWatched(movie.ids.trakt, MediaType.Movie, null),
                            watching = state.collection.isWatching(movie.ids.trakt, MediaType.Movie, null),
                            watchlist = state.collection.isWatchlist(movie.ids.trakt, MediaType.Movie),
                            onClick = {
                                if (!state.isLoadingPage) {
                                    onMovieClick(movie.ids.trakt)
                                }
                            },
                            chipContent = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(1.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_movies_off),
                                            contentDescription = null,
                                            tint = TraktTheme.colors.textPrimary,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .graphicsLayer {
                                                    translationY = -0.5.dp.toPx()
                                                },
                                        )

                                        Text(
                                            text = movie.yearString +
                                                "  •  ${rememberDurationFormat(movie.runtime?.inWholeMinutes)}",
                                            style = TraktTheme.typography.cardTitle,
                                            color = TraktTheme.colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        focusedItem = item
                                        focusedItemId = item.id

                                        loadNextPageIfNeeded(
                                            size = state.items.size,
                                            index = index,
                                            onLoadNextPage = onLoadNextPage,
                                        )
                                    }
                                },
                        )
                    }
                }
            } else if (!state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    TvListEmptyState(
                        filter = state.filter,
                        defaultMessageRes = R.string.list_placeholder_personal_list_empty,
                        modifier = Modifier.focusable(),
                    )
                }
            }

            if (state.isLoadingPage) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            }
        }
    }

    if (state.error != null) {
        GenericErrorView(
            error = state.error,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
        )
    }
}

private fun loadNextPageIfNeeded(
    size: Int,
    index: Int,
    onLoadNextPage: () -> Unit,
) {
    if (index >= (size - PERSONAL_LIST_NEXT_PAGE_OFFSET).coerceAtLeast(0)) {
        onLoadNextPage()
    }
}

@Preview(
    name = "4K",
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        PersonalListContent(
            listName = "Personal List",
            state = PersonalListState(
//                shows = (1..20).map {
//                    PreviewData.show1.copy(ids = Ids(TraktId(it), SlugId(it.toString())))
//                }.toImmutableList(),
            ),
            onShowClick = {},
            onMovieClick = {},
            onLoadNextPage = {},
            onFilterApplied = {},
            onSortingApplied = {},
        )
    }
}
