package tv.trakt.trakt.app.core.lists.details.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator

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
) {
    var focusedItem by remember { mutableStateOf<PersonalListItem?>(null) }
    var focusedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(Unit) {
        delay(500)
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
                Text(
                    text = listName,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .focusProperties {
                            down = focusRequesters.values.firstOrNull() ?: FocusRequester.Default
                        }
                        .focusable(),
                )
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
                    val focusRequester = focusRequesters.getOrPut(item.id) {
                        FocusRequester()
                    }

                    item.show?.let { show ->
                        VerticalMediaCard(
                            title = show.title,
                            imageUrl = show.images?.getPosterUrl(),
                            onClick = {
                                if (!state.isLoadingPage) {
                                    onShowClick(show.ids.trakt)
                                }
                            },
                            chipContent = {
                                val episodes = show.airedEpisodes
                                if (episodes > 0) {
                                    InfoChip(
                                        text = stringResource(tv.trakt.trakt.common.R.string.episodes_count, episodes),
                                    )
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
                            onClick = {
                                if (!state.isLoadingPage) {
                                    onMovieClick(movie.ids.trakt)
                                }
                            },
                            chipContent = {
                                movie.runtime?.inWholeMinutes?.let {
                                    InfoChip(text = it.durationFormat())
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
    if (size >= PERSONAL_LIST_PAGE_LIMIT && index >= size - PERSONAL_LIST_NEXT_PAGE_OFFSET) {
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
        )
    }
}
