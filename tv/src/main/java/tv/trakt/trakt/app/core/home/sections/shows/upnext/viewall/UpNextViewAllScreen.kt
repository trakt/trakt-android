package tv.trakt.trakt.app.core.home.sections.shows.upnext.viewall

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import tv.trakt.trakt.app.common.ui.EpisodeProgressBar
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.chips.FinaleChip
import tv.trakt.trakt.app.common.ui.chips.PremiereChip
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_PAGE_LIMIT
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressItem
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressMovie
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressShow
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images.Size.FULL
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun UpNextViewAllScreen(
    viewModel: UpNextViewAllViewModel,
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    UpNextViewAllContent(
        state = state,
        onShowClick = { show, episode ->
            onNavigateToEpisode(show.ids.trakt, episode)
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
private fun UpNextViewAllContent(
    state: UpNextViewAllState,
    modifier: Modifier = Modifier,
    onShowClick: (Show, Episode) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedItem by remember { mutableStateOf<ProgressItem?>(null) }
    var focusedItemId by rememberSaveable { mutableStateOf<Int?>(null) }
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    LaunchedEffect(state.isLoading) {
        // Used when list is updated after user comes back and modifies history/watchlist etc.
        if (state.isLoading) {
            focusedItem = null
            focusedItemId = null
            focusRequesters.clear()
        }
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
            imageUrl = when (val item = focusedItem) {
                is ProgressShow -> item.progress.nextEpisode?.images?.getScreenshotUrl(FULL)
                is ProgressMovie -> item.movie.images?.getFanartUrl(FULL)
                else -> null
            },
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = spacedBy(gridSpace),
            verticalArrangement = spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 16.dp,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.list_title_up_next),
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

                    val focusRequester = remember {
                        focusRequesters.getOrPut(item.id.value) {
                            FocusRequester()
                        }
                    }

                    if (item is ProgressMovie) {
                        HorizontalMediaCard(
                            title = "",
                            containerImageUrl = item.movie.images?.getFanartUrl(),
                            onClick = { onMovieClick(item.movie) },
                            cardContent = {
                                Column(
                                    verticalArrangement = spacedBy(3.dp),
                                ) {
                                    Row(
                                        horizontalArrangement = spacedBy(2.dp),
                                    ) {
                                        val remainingPercent = remember(item.progress.progress) {
                                            (100F - item.progress.progress) / 100F
                                        }

                                        EpisodeProgressBar(
                                            startText = stringResource(
                                                R.string.tag_text_remaining_duration,
                                                item.remainingTimeText() ?: "?",
                                            ),
                                            containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                                            progress = (1F - remainingPercent).coerceIn(0F, 0.99F),
                                        )
                                    }
                                }
                            },
                            footerContent = {
                                Column(
                                    verticalArrangement = spacedBy(1.dp),
                                ) {
                                    Text(
                                        text = item.movie.title,
                                        style = TraktTheme.typography.cardTitle,
                                        color = TraktTheme.colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )

                                    Text(
                                        text = rememberDurationFormat(item.movie.runtime?.inWholeMinutes),
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
                                        focusedItemId = item.id.value

                                        loadNextPageIfNeeded(
                                            size = state.items.size,
                                            index = index,
                                            onLoadNextPage = onLoadNextPage,
                                        )
                                    }
                                },
                        )
                    } else if (item is ProgressShow) {
                        HorizontalMediaCard(
                            title = "",
                            containerImageUrl =
                                item.show.images?.getFanartUrl()
                                    ?: item.progress.nextEpisode?.images?.getScreenshotUrl(),
                            onClick = {
                                item.progress.nextEpisode?.let {
                                    onShowClick(item.show, it)
                                }
                            },
                            cardContent = {
                                Column(
                                    verticalArrangement = spacedBy(3.dp),
                                ) {
                                    when {
                                        item.progress.nextEpisode?.isPremiere(
                                            item.progress.isLatestAired,
                                        ) == true -> PremiereChip()
                                        item.progress.nextEpisode?.isFinale(
                                            item.progress.isLatestAired,
                                        ) == true -> FinaleChip()
                                    }

                                    val remainingEpisodes = remember(item.progress.completed, item.progress.aired) {
                                        item.progress.remainingEpisodes
                                    }

                                    val remainingPercent = remember(item.progress.completed, item.progress.aired) {
                                        item.progress.remainingPercent
                                    }

                                    EpisodeProgressBar(
                                        startText = rememberDurationFormat(
                                            item.progress.nextEpisode?.runtime?.inWholeMinutes,
                                        ),
                                        endText = stringResource(
                                            R.string.tag_text_remaining_episodes,
                                            remainingEpisodes,
                                        ),
                                        containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                                        progress = remainingPercent,
                                    )
                                }
                            },
                            footerContent = {
                                Column(
                                    verticalArrangement = spacedBy(1.dp),
                                ) {
                                    Text(
                                        text = item.show.title,
                                        style = TraktTheme.typography.cardTitle,
                                        color = TraktTheme.colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )

                                    Text(
                                        text = item.progress.nextEpisode?.seasonEpisodeString() ?: "N/A",
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
                                        focusedItemId = item.id.value

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
    if (size >= HOME_PAGE_LIMIT && index >= size - HOME_NEXT_PAGE_OFFSET) {
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
        UpNextViewAllContent(
            state = UpNextViewAllState(),
            onShowClick = { _, _ -> },
            onMovieClick = {},
            onLoadNextPage = {},
        )
    }
}
