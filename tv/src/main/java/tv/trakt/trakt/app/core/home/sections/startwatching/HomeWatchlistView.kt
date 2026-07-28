package tv.trakt.trakt.app.core.home.sections.startwatching

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalViewAllCard
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem.MovieItem
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem.ShowItem
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

@Composable
internal fun HomeWatchlistView(
    modifier: Modifier = Modifier,
    viewModel: HomeWatchlistViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (WatchlistItem?) -> Unit = {},
    onLoaded: () -> Unit = {},
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToViewAll: () -> Unit,
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

    HomeWatchlistContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onFocused = onFocused,
        onNavigateToShow = onNavigateToShow,
        onNavigateToMovie = onNavigateToMovie,
        onNavigateToViewAll = onNavigateToViewAll,
    )
}

@Composable
internal fun HomeWatchlistContent(
    state: HomeWatchlistState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (WatchlistItem?) -> Unit = {},
    onNavigateToShow: (TraktId) -> Unit = {},
    onNavigateToMovie: (TraktId) -> Unit = {},
    onNavigateToViewAll: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.list_title_start_watching),
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
                    onShowClick = { onNavigateToShow(it.ids.trakt) },
                    onMovieClick = { onNavigateToMovie(it.ids.trakt) },
                    onViewAllClick = onNavigateToViewAll,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<WatchlistItem>,
    onFocused: (WatchlistItem) -> Unit,
    onShowClick: (Show) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            VerticalMediaCard(
                width = TraktTheme.size.verticalMediaBigCardSize,
                title = item.title,
                imageUrl = item.posterImage,
                onClick = {
                    when (item) {
                        is ShowItem -> onShowClick(item.show)
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
                    .onFocusChanged {
                        if (it.isFocused) onFocused(item)
                    },
            )
        }

        if (listItems.size >= HOME_SECTION_LIMIT) {
            item {
                VerticalViewAllCard(
                    width = TraktTheme.size.verticalMediaBigCardSize,
                    onClick = onViewAllClick,
                )
            }
        }

        emptyFocusListItems()
    }
}

@Composable
private fun ContentLoadingList(
    contentPadding: PaddingValues,
    onFocused: () -> Unit,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
        modifier = Modifier
            .padding(bottom = 10.dp),
    ) {
        items(count = 10) {
            VerticalMediaSkeletonCard(
                width = TraktTheme.size.verticalMediaBigCardSize,
                modifier = Modifier
                    .onFocusChanged {
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
        HomeWatchlistContent(
            state = HomeWatchlistState(
                items = emptyList<WatchlistItem>().toImmutableList(),
                isLoading = false,
                error = null,
            ),
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        HomeWatchlistContent(
            state = HomeWatchlistState(),
        )
    }
}
