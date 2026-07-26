package tv.trakt.trakt.app.core.lists.details.shows

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.lists.filters.TvListControls
import tv.trakt.trakt.app.core.lists.filters.TvListControlsState
import tv.trakt.trakt.app.core.lists.filters.TvListEmptyState
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun ShowsWatchlistScreen(
    viewModel: ShowsWatchlistViewModel,
    onNavigateToShow: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowsWatchlistContent(
        state = state,
        onShowClick = onNavigateToShow,
        onLoadNextPage = { viewModel.loadNextDataPage() },
        onFilterApplied = viewModel::applyFilter,
        onSortingApplied = viewModel::applySorting,
    )
}

@Composable
private fun ShowsWatchlistContent(
    state: ShowsWatchlistState,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
    onLoadNextPage: () -> Unit,
    onFilterApplied: (GlobalFilter) -> Unit,
    onSortingApplied: (Sorting) -> Unit,
) {
    var focusedShow by remember { mutableStateOf<Show?>(null) }
    var focusedShowId by rememberSaveable { mutableStateOf<Int?>(null) }
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedShowId]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedShow?.images?.getFanartUrl(Images.Size.FULL),
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
                        text = stringResource(R.string.list_title_watchlist_shows),
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
                            configuration = TvListFilterConfiguration.ShowsWatchlist,
                        ),
                        onFilterApplied = onFilterApplied,
                        onSortingApplied = onSortingApplied,
                    )
                }
            }

            if (state.isLoading && state.shows.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.shows.isNullOrEmpty()) {
                items(
                    count = state.shows.size,
                    key = { index -> state.shows[index].ids.trakt.value },
                ) { index ->
                    val show = state.shows[index]
                    val focusRequester = remember(show.ids.trakt.value) {
                        focusRequesters.getOrPut(show.ids.trakt.value) {
                            FocusRequester()
                        }
                    }

                    VerticalMediaCard(
                        title = show.title,
                        imageUrl = show.images?.getPosterUrl(),
                        onClick = {
                            if (!state.isLoadingPage) {
                                onShowClick(show.ids.trakt)
                            }
                        },
                        chipContent = {
                            InfoChip(
                                text = stringResource(R.string.tag_text_number_of_episodes, show.airedEpisodes),
                            )
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedShow = show
                                    focusedShowId = show.ids.trakt.value

                                    loadNextPageIfNeeded(
                                        size = state.shows.size,
                                        index = index,
                                        onLoadNextPage = onLoadNextPage,
                                    )
                                }
                            },
                    )
                }
            } else if (!state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    TvListEmptyState(
                        filter = state.filter,
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
    if (index >= (size - LISTS_NEXT_PAGE_OFFSET).coerceAtLeast(0)) {
        onLoadNextPage()
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1000,
)
@Composable
private fun Preview() {
    TraktTheme {
        ShowsWatchlistContent(
            state = ShowsWatchlistState(
                shows = (1..20).map {
                    PreviewData.show1.copy(ids = Ids(TraktId(it), SlugId(it.toString())))
                }.toImmutableList(),
            ),
            onShowClick = {},
            onLoadNextPage = {},
            onFilterApplied = {},
            onSortingApplied = {},
        )
    }
}
