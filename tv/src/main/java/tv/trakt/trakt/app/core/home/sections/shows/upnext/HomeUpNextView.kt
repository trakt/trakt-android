package tv.trakt.trakt.app.core.home.sections.shows.upnext

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.ui.EpisodeProgressBar
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.chips.FinaleChip
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.chips.PremiereChip
import tv.trakt.trakt.app.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressItem
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressMovie
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressShow
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

private val sections = listOf(
    "initial",
    "content",
)

@Composable
internal fun HomeUpNextView(
    modifier: Modifier = Modifier,
    viewModel: HomeUpNextViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (Images?) -> Unit = {},
    onLoaded: () -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (movieId: TraktId) -> Unit,
    onNavigateToViewAll: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.items != null) {
            onLoaded()
        }
    }

    HomeUpNextContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        focusRequesters = focusRequesters,
        onFocused = onFocused,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateToMovie = onNavigateToMovie,
        onViewAllClick = onNavigateToViewAll,
    )
}

@Composable
internal fun HomeUpNextContent(
    state: HomeUpNextState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
    onFocused: (Images?) -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onNavigateToMovie: (movieId: TraktId) -> Unit = {},
    onViewAllClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.list_title_up_next),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(headerPadding),
        )

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                    onFocused = { onFocused(null) },
                )
            }

            state.items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier
                        .padding(headerPadding),
                )
            }

            else -> {
                ContentList(
                    listItems = { state.items ?: EmptyImmutableList },
                    onFocused = onFocused,
                    onShowClick = { show, episode ->
                        onNavigateToEpisode(show.ids.trakt, episode)
                    },
                    onMovieClick = {
                        onNavigateToMovie(it.ids.trakt)
                    },
                    onViewAllClick = onViewAllClick,
                    contentPadding = contentPadding,
                    focusRequesters = focusRequesters,
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: () -> ImmutableList<ProgressItem>,
    onFocused: (Images?) -> Unit,
    onShowClick: (Show, Episode) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
        modifier = Modifier
            .focusRequester(focusRequesters["content"] ?: FocusRequester.Default),
    ) {
        items(
            items = listItems(),
            key = { it.key },
        ) { item ->
            if (item is ProgressShow) {
                ContentShowListItem(
                    item = item,
                    onClick = onShowClick,
                    onFocused = onFocused,
                )
            } else if (item is ProgressMovie) {
                ContentMovieListItem(
                    item = item,
                    onClick = onMovieClick,
                    onFocused = onFocused,
                )
            }
        }

        if (listItems().size >= HOME_SECTION_LIMIT) {
            item {
                HorizontalViewAllCard(
                    onClick = onViewAllClick,
                )
            }
        }

        emptyFocusListItems()
    }
}

@Composable
private fun ContentShowListItem(
    item: ProgressShow,
    onClick: (Show, Episode) -> Unit,
    onFocused: (Images?) -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl =
            item.show.images?.getFanartUrl()
                ?: item.progress.nextEpisode?.images?.getScreenshotUrl(),
        onClick = {
            item.progress.nextEpisode?.let {
                onClick(item.show, it)
            }
        },
        cardContent = {
            Column(
                verticalArrangement = spacedBy(3.dp),
            ) {
                when {
                    item.progress.nextEpisode?.isPremiere() == true -> PremiereChip()
                    item.progress.nextEpisode?.isFinale() == true -> FinaleChip()
                }

                Row(
                    horizontalArrangement = spacedBy(2.dp),
                ) {
                    val runtime = item.progress.nextEpisode?.runtime?.inWholeMinutes
                    if (runtime != null) {
                        InfoChip(
                            text = rememberDurationFormat(runtime),
                            containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                        )
                    }

                    val remainingEpisodes = remember(item.progress.completed, item.progress.aired) {
                        item.progress.remainingEpisodes
                    }
                    val remainingPercent = remember(item.progress.completed, item.progress.aired) {
                        item.progress.remainingPercent
                    }

                    EpisodeProgressBar(
                        startText = stringResource(R.string.tag_text_remaining_episodes, remainingEpisodes),
                        containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                        progress = remainingPercent,
                    )
                }
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
            .onFocusChanged {
                if (it.isFocused) onFocused(item.show.images)
            },
    )
}

@Composable
private fun ContentMovieListItem(
    item: ProgressMovie,
    onClick: (Movie) -> Unit,
    onFocused: (Images?) -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = item.movie.images?.getFanartUrl(),
        onClick = { onClick(item.movie) },
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
            .onFocusChanged {
                if (it.isFocused) onFocused(item.movie.images)
            },
    )
}

@Composable
private fun ContentLoadingList(
    contentPadding: PaddingValues,
    onFocused: () -> Unit,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            EpisodeSkeletonCard(
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) {
                        onFocused()
                    }
                },
            )
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
        HomeUpNextContent(
            state = HomeUpNextState(),
        )
    }
}
