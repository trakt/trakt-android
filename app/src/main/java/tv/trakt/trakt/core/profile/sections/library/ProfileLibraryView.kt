@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.library

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.library.model.LibraryFilter
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.core.profile.sections.favorites.context.movie.FavoriteMovieContextSheet
import tv.trakt.trakt.core.profile.sections.favorites.context.show.FavoriteShowContextSheet
import tv.trakt.trakt.core.profile.sections.library.views.LibraryItemView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileLibraryView(
    modifier: Modifier = Modifier,
    viewModel: ProfileLibraryViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onMoreClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showContextSheet by remember { mutableStateOf<Show?>(null) }
    var movieContextSheet by remember { mutableStateOf<Movie?>(null) }

    ProfileLibraryView(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onFilterClick = viewModel::setFilter,
        onEpisodeClick = { show, _ ->
            scope.launch {
                viewModel.onNavigateToShow(show)
                onShowClick(show.ids.trakt)
            }
        },
        onMovieClick = {
            scope.launch {
                viewModel.onNavigateToMovie(it)
                onMovieClick(it.ids.trakt)
            }
        },
        onLibraryClick = {
            if (state.loading == DONE && !state.items.isNullOrEmpty()) {
                onMoreClick()
            }
        },
    )

    FavoriteShowContextSheet(
        show = showContextSheet,
        onDismiss = { showContextSheet = null },
    )

    FavoriteMovieContextSheet(
        movie = movieContextSheet,
        onDismiss = { movieContextSheet = null },
    )
}

@Composable
private fun ProfileLibraryView(
    state: ProfileLibraryState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (collapsed: Boolean) -> Unit = {},
    onFilterClick: (LibraryFilter) -> Unit = {},
    onEpisodeClick: (Show, Episode) -> Unit = { _, _ -> },
    onMovieClick: (Movie) -> Unit = {},
    onLibraryClick: () -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_library),
            chevron = !state.items.isNullOrEmpty() || state.loading != DONE,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onLibraryClick()
                },
        )

        if (state.collapsed != true) {
            ContentFilters(
                state = state,
                headerPadding = headerPadding,
                onFilterClick = onFilterClick,
            )

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
                                        "${
                                            stringResource(
                                                R.string.error_text_unexpected_error_short,
                                            )
                                        }\n\n${state.error}",
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.meta,
                                    maxLines = 10,
                                    modifier = Modifier.padding(contentPadding),
                                )
                            }

                            state.items?.isEmpty() == true -> {
                                Text(
                                    text = stringResource(R.string.list_placeholder_empty),
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.heading6,
                                    modifier = Modifier.padding(contentPadding),
                                )
                            }

                            else -> {
                                ContentList(
//                                filter = state.filter,
                                    listItems = (state.items ?: emptyList()).toImmutableList(),
                                    contentPadding = contentPadding,
                                    onEpisodeClick = {
                                        onEpisodeClick(it.show, it.episode)
                                    },
                                    onMovieClick = {
                                        onMovieClick(it.movie)
                                    },
                                )
                            }
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
    state: ProfileLibraryState,
    modifier: Modifier = Modifier,
    onFilterClick: (LibraryFilter) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = headerPadding,
        paddingVertical = PaddingValues(top = 13.dp, bottom = 15.dp),
        modifier = modifier,
    ) {
        for (filter in LibraryFilter.entries) {
            FilterChip(
                selected = filter == state.filter,
                text = stringResource(filter.displayRes),
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun ContentLoadingList(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    Row(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        modifier = modifier
            .wrapContentWidth(align = Alignment.Start, unbounded = true)
            .padding(contentPadding)
            .alpha(if (visible) 1F else 0F),
    ) {
        repeat(6) {
            VerticalMediaSkeletonCard(
                chipRatio = 0.66F,
                secondaryChip = true,
            )
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<LibraryItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onEpisodeClick: (LibraryItem.EpisodeItem) -> Unit = {},
    onMovieClick: (LibraryItem.MovieItem) -> Unit = {},
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
            LibraryItemView(
                item = item,
                onEpisodeClick = {
                    (item as? LibraryItem.EpisodeItem)?.let {
                        onEpisodeClick(item)
                    }
                },
                onMovieClick = {
                    (item as? LibraryItem.MovieItem)?.let {
                        onMovieClick(item)
                    }
                },
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
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
        ProfileLibraryView(
            state = ProfileLibraryState(
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
        ProfileLibraryView(
            state = ProfileLibraryState(
                loading = LOADING,
            ),
        )
    }
}
