package tv.trakt.trakt.core.lists.sections.watchlist

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistFilter
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListsWatchlistView(
    modifier: Modifier = Modifier,
    viewModel: ListsWatchlistViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeActivityContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onFilterClick = viewModel::setFilter,
    )
}

@Composable
internal fun HomeActivityContent(
    state: ListsWatchlistState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFilterClick: (WatchlistFilter) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.page_title_watchlist),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading5,
                )
                Text(
                    text = "Recently added", // TODO String
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(fontWeight = W400),
                )
            }
            if (!state.items.isNullOrEmpty() || state.loading != DONE) {
                Text(
                    text = stringResource(R.string.button_text_view_all),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.buttonSecondary,
                )
            }
        }

        if (!state.items.isNullOrEmpty() || state.loading.isLoading || state.user != null) {
            ContentFilters(
                state = state,
                headerPadding = headerPadding,
                onFilterClick = onFilterClick,
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    when {
                        state.error != null -> {
                            Text(
                                text =
                                    "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.meta,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }
                        state.items?.isEmpty() == true -> {
//                            HomeEmptySocialView(
//                                modifier = Modifier
//                                    .padding(contentPadding),
//                            )
                        }
                        else -> {
                            ContentList(
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentFilters(
    headerPadding: PaddingValues,
    state: ListsWatchlistState,
    onFilterClick: (WatchlistFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(headerPadding)
            .padding(
                top = 13.dp,
                bottom = 15.dp,
            ),
        horizontalArrangement = spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (filter in WatchlistFilter.entries) {
            FilterChip(
                selected = state.filter == filter,
                text = stringResource(filter.displayRes),
                leadingIcon = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun ContentLoadingList(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<WatchlistItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
//    onEpisodeClick: (TraktId, Episode) -> Unit,
//    onMovieClick: (TraktId) -> Unit,
) {
    val currentList = remember { mutableIntStateOf(listItems.hashCode()) }

    LaunchedEffect(listItems) {
        val hashCode = listItems.hashCode()
        if (currentList.intValue != hashCode) {
            currentList.intValue = hashCode
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            when (item) {
                is WatchlistItem.ShowItem -> {
                    VerticalMediaCard(
                        title = item.show.title,
                        imageUrl = item.images?.getPosterUrl(),
                        chipContent = {
                            if (item.show.airedEpisodes > 0) {
                                InfoChip(
                                    text = stringResource(
                                        R.string.tag_text_number_of_episodes,
                                        item.show.airedEpisodes,
                                    ),
                                )
                            }
                        },
                    )
                }
                is WatchlistItem.MovieItem -> {
                    VerticalMediaCard(
                        title = item.movie.title,
                        imageUrl = item.images?.getPosterUrl(),
                        chipContent = {
                            Row(
                                horizontalArrangement = spacedBy(5.dp),
                            ) {
                                if ((item.movie.year ?: 0) > 0) {
                                    InfoChip(
                                        text = item.movie.year.toString(),
                                    )
                                }
                                item.movie.runtime?.inWholeMinutes?.let {
                                    val runtimeString = remember(item.movie.runtime) {
                                        it.durationFormat()
                                    }
                                    InfoChip(
                                        text = runtimeString,
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

// Previews

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeActivityContent(
            state = ListsWatchlistState(
                loading = IDLE,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        HomeActivityContent(
            state = ListsWatchlistState(
                loading = LOADING,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        HomeActivityContent(
            state = ListsWatchlistState(
                loading = DONE,
                items = emptyList<WatchlistItem>().toImmutableList(),
            ),
        )
    }
}
