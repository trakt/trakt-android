package tv.trakt.trakt.app.core.home.sections.startwatching.viewall

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_WATCHLIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem.MovieItem
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem.ShowItem
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun WatchlistViewAllScreen(
    viewModel: WatchlistViewAllViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    WatchlistViewAllContent(
        state = state,
        onEpisodeClick = {
            onNavigateToShow(it.ids.trakt)
        },
        onMovieClick = {
            onNavigateToMovie(it.ids.trakt)
        },
        onLoadNextPage = {
            viewModel.loadNextDataPage()
        },
    )
}

@Composable
private fun WatchlistViewAllContent(
    state: WatchlistViewAllState,
    modifier: Modifier = Modifier,
    onEpisodeClick: (Show) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedItem by remember { mutableStateOf<WatchlistItem?>(null) }
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
            imageUrl = focusedItem?.fullFanartImage,
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = TraktTheme.size.verticalMediaCardSize),
            horizontalArrangement = spacedBy(gridSpace),
            verticalArrangement = spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.list_title_start_watching),
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
                    key = { index -> state.items[index].key },
                ) { index ->
                    val item = state.items[index]

                    val focusRequester = remember(item.key) {
                        focusRequesters.getOrPut(item.key) {
                            FocusRequester()
                        }
                    }

                    VerticalMediaCard(
                        width = TraktTheme.size.verticalMediaCardSize,
                        title = item.title,
                        imageUrl = item.posterImage,
                        onClick = {
                            when (item) {
                                is ShowItem -> onEpisodeClick(item.show)
                                is MovieItem -> onMovieClick(item.movie)
                            }
                        },
                        chipContent = {
                            val subtitle = when (item) {
                                is ShowItem -> {
                                    stringResource(R.string.episode_footer_season_episode, 1, 1)
                                }
                                is MovieItem -> {
                                    rememberDurationFormat(item.movie.runtime?.inWholeMinutes)
                                }
                            }

                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                Text(
                                    text = item.title,
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = subtitle,
                                    style = TraktTheme.typography.cardSubtitle,
                                    color = TraktTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem = item
                                    focusedItemId = item.key

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
    if (size >= HOME_WATCHLIST_PAGE_LIMIT && index >= size - HOME_WATCHLIST_PAGE_LIMIT) {
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
        WatchlistViewAllContent(
            state = WatchlistViewAllState(),
            onEpisodeClick = {},
            onMovieClick = {},
            onLoadNextPage = {},
        )
    }
}
