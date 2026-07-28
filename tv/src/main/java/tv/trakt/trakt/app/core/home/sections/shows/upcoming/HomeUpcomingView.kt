package tv.trakt.trakt.app.core.home.sections.shows.upcoming

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
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
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.chips.FinaleChip
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.chips.PremiereChip
import tv.trakt.trakt.app.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

@Composable
internal fun HomeUpcomingView(
    modifier: Modifier = Modifier,
    viewModel: HomeUpcomingViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (HomeUpcomingItem?) -> Unit = {},
    onLoaded: () -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (movieId: TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.items != null) {
            onLoaded()
        }
    }

    HomeUpcomingContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onFocused = onFocused,
        onNavigateToMovie = onNavigateToMovie,
        onNavigateToEpisode = onNavigateToEpisode,
    )
}

@Composable
internal fun HomeUpcomingContent(
    state: HomeUpcomingState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (HomeUpcomingItem?) -> Unit = {},
    onNavigateToMovie: (movieId: TraktId) -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.list_title_upcoming_schedule),
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
                    listItems = state.items ?: EmptyImmutableList,
                    onFocused = onFocused,
                    onClick = {
                        when (it) {
                            is HomeUpcomingItem.EpisodeItem -> {
                                onNavigateToEpisode(
                                    it.show.ids.trakt,
                                    it.episode,
                                )
                            }
                            is HomeUpcomingItem.MovieItem -> {
                                onNavigateToMovie(
                                    it.movie.ids.trakt,
                                )
                            }
                        }
                    },
                    contentPadding = contentPadding,
                )
            }
        }
    }
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

@Composable
private fun ContentList(
    listItems: ImmutableList<HomeUpcomingItem>,
    onFocused: (HomeUpcomingItem) -> Unit,
    onClick: (HomeUpcomingItem) -> Unit,
    contentPadding: PaddingValues,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            ContentListItem(
                item = item,
                onFocused = onFocused,
                onClick = onClick,
            )
        }

        emptyFocusListItems()
    }
}

@Composable
private fun ContentListItem(
    item: HomeUpcomingItem,
    onFocused: (HomeUpcomingItem) -> Unit,
    onClick: (HomeUpcomingItem) -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = item.images?.getFanartUrl(),
        onClick = { onClick(item) },
        cardContent = {
            Column(
                verticalArrangement = spacedBy(2.dp),
            ) {
                if (item is HomeUpcomingItem.EpisodeItem) {
                    when {
                        item.episode.isPremiere() -> PremiereChip()
                        item.episode.isFinale() -> FinaleChip()
                    }
                }

                val isReleased = when (item) {
                    is HomeUpcomingItem.EpisodeItem -> item.episode.isReleased
                    is HomeUpcomingItem.MovieItem -> item.movie.isReleased
                }

                InfoChip(
                    text = item.releaseAt?.toLocal()?.relativeDateTimeString() ?: "TBA",
                    iconPainter = when {
                        isReleased -> painterResource(R.drawable.ic_calendar_check)
                        else -> painterResource(R.drawable.ic_calendar_upcoming)
                    },
                    containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                )
            }
        },
        footerContent = {
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

                val subtitle = when (item) {
                    is HomeUpcomingItem.EpisodeItem -> {
                        when {
                            item.isFullSeason -> stringResource(
                                R.string.text_season_number,
                                item.episode.season,
                            )
                            item.episodes.size > 1 -> stringResource(
                                R.string.episode_footer_season_episode_range,
                                item.episode.season,
                                item.episodes.first().number,
                                item.episodes.last().number,
                            )
                            else -> item.episode.seasonEpisodeString()
                        }
                    }
                    is HomeUpcomingItem.MovieItem -> {
                        rememberDurationFormat(item.movie.runtime?.inWholeMinutes)
                    }
                }

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
            .onFocusChanged {
                if (it.isFocused) {
                    onFocused(item)
                }
            },
    )
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeUpcomingContent(
            state = HomeUpcomingState(),
        )
    }
}
