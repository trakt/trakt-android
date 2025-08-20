package tv.trakt.trakt.app.core.home.sections.movies.availablenow

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.movies.availablenow.model.WatchlistMovie
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

@Composable
internal fun HomeAvailableNowView(
    modifier: Modifier = Modifier,
    viewModel: HomeAvailableNowViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (Movie) -> Unit = {},
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToViewAll: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    HomeAvailableNowContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onFocused = onFocused,
        onNavigateToMovie = onNavigateToMovie,
        onNavigateToViewAll = onNavigateToViewAll,
    )
}

@Composable
internal fun HomeAvailableNowContent(
    state: HomeAvailableNowState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (Movie) -> Unit = {},
    onNavigateToMovie: (TraktId) -> Unit = {},
    onNavigateToViewAll: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.list_title_available_now),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(headerPadding),
        )

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                )
            }

            state.movies?.isEmpty() == true -> {
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
                    listItems = { state.movies ?: emptyList<WatchlistMovie>().toImmutableList() },
                    onFocused = onFocused,
                    onClick = { onNavigateToMovie(it.ids.trakt) },
                    onViewAllClick = onNavigateToViewAll,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: () -> ImmutableList<WatchlistMovie>,
    onFocused: (Movie) -> Unit,
    onClick: (Movie) -> Unit,
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems(),
            key = { it.movie.ids.trakt.value },
        ) { item ->
            HorizontalMediaCard(
                title = item.movie.title,
                containerImageUrl = item.movie.images?.getFanartUrl(),
                contentImageUrl = item.movie.images?.getLogoUrl(),
                paletteColor = item.movie.colors?.colors?.second,
                onClick = { onClick(item.movie) },
                footerContent = {
                    val runtime = item.movie.runtime?.inWholeMinutes
                    if (runtime != null) {
                        InfoChip(
                            text = runtime.durationFormat(),
                        )
                    }
                },
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) onFocused(item.movie)
                    },
            )
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
private fun ContentLoadingList(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            HorizontalMediaSkeletonCard()
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
        HomeAvailableNowContent(
            state = HomeAvailableNowState(
                movies = emptyList<WatchlistMovie>().toImmutableList(),
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
        HomeAvailableNowContent(
            state = HomeAvailableNowState(),
        )
    }
}
