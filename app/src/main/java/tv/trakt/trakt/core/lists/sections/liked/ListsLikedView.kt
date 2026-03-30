package tv.trakt.trakt.core.lists.sections.liked

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.sheet.ListMovieContextSheet
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.sheet.ListShowContextSheet
import tv.trakt.trakt.core.lists.sections.personal.ui.ListsCustomItemView
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListsLikedView(
    viewModel: ListsLikedViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onAllClick: (CustomList) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateMovie, state.navigateShow, state.navigateEpisode) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let { (showId, episode) ->
            onEpisodeClick(showId, episode)
            viewModel.clearNavigation()
        }
    }

    var showContextSheet by remember { mutableStateOf<Show?>(null) }
    var movieContextSheet by remember { mutableStateOf<Movie?>(null) }

    ListsLikedContent(
        state = state,
        list = state.list,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onMovieClick = {
            if (!state.loading.isLoading) {
                viewModel.navigateToMovie(it)
            }
        },
        onShowClick = {
            if (!state.loading.isLoading) {
                viewModel.navigateToShow(it)
            }
        },
        onEpisodeClick = { showId, episode ->
            if (!state.loading.isLoading) {
                viewModel.navigateToEpisode(showId, episode)
            }
        },
        onAllClick = {
            state.list?.let {
                if (!state.items.isNullOrEmpty()) {
                    onAllClick(it)
                }
            }
        },
    )

    ListShowContextSheet(
        show = showContextSheet,
        list = state.list,
        onDismiss = { showContextSheet = null },
    )

    ListMovieContextSheet(
        movie = movieContextSheet,
        list = state.list,
        onDismiss = { movieContextSheet = null },
    )
}

@Composable
private fun ListsLikedContent(
    state: ListsLikedState,
    list: CustomList?,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (collapsed: Boolean) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onEpisodeClick: (Show, Episode) -> Unit = { _, _ -> },
    onAllClick: () -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = list?.name ?: "",
            subtitle = when {
                !list?.description.isNullOrBlank() -> list.description
                else -> null
            },
            chevron = !state.items.isNullOrEmpty() || state.loading != Done,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            maxSubtitleLength = 30,
            modifier = Modifier
                .padding(headerPadding)
                .onClick(enabled = state.loading == Done) {
                    onAllClick()
                },
        )

        if (state.collapsed != true) {
            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
            ) { loading ->
                when (loading) {
                    Idle, Loading -> {
                        ContentLoadingList(
                            visible = loading.isLoading,
                            contentPadding = contentPadding,
                            modifier = Modifier.padding(bottom = 3.75.dp),
                        )
                    }

                    Done -> {
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
                                ContentEmptyList(
                                    filter = state.filter,
                                    contentPadding = contentPadding,
                                    modifier = Modifier.padding(bottom = 3.75.dp),
                                )
                            }

                            else -> {
                                ContentList(
                                    listItems = (state.items ?: emptyList()).toImmutableList(),
                                    listFilter = state.filter,
                                    collectionState = state.collection,
                                    contentPadding = contentPadding,
                                    onMovieClick = onMovieClick,
                                    onShowClick = onShowClick,
                                    onEpisodeClick = onEpisodeClick,
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
private fun ContentLoadingList(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 12) {
            VerticalMediaSkeletonCard(
                chipRatio = 0.66F,
                chipSpacing = 8.dp,
                shimmer = false,
            )
        }
    }
}

@Composable
private fun ContentEmptyList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    filter: MediaMode?,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(contentPadding)
            .background(
                color = TraktTheme.colors.skeletonContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .fillMaxWidth()
            .height(217.dp),
    ) {
        Text(
            text = stringResource(
                when (filter) {
                    MediaMode.MOVIES -> R.string.list_placeholder_personal_list_empty_movies
                    MediaMode.SHOWS -> R.string.list_placeholder_personal_list_empty_shows
                    else -> R.string.list_placeholder_empty
                },
            ),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading6,
            modifier = Modifier
                .padding(20.dp),
        )
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<CustomListItem>,
    listState: LazyListState = rememberLazyListState(),
    listFilter: MediaMode?,
    collectionState: UserCollectionState,
    contentPadding: PaddingValues,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onEpisodeClick: (Show, Episode) -> Unit = { _, _ -> },
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
            ListsCustomItemView(
                item = item,
                showMediaIcon = listFilter == MediaMode.MEDIA,
                showMoreIcon = false,
                watched = collectionState.isWatched(item.id, item.type),
                watchlist = collectionState.isWatchlist(item.id, item.type),
                onMovieClick = onMovieClick,
                onShowClick = onShowClick,
                onEpisodeClick = onEpisodeClick,
                onLongClick = null,
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
    locale = "us",
)
@Composable
private fun PreviewLoadingState() {
    TraktTheme {
        ContentLoadingList(
            visible = true,
            contentPadding = PaddingValues(16.dp),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun PreviewListState() {
    TraktTheme {
        ListsLikedContent(
            state = ListsLikedState(),
            list = PreviewData.customList1.copy(
                name = "Lorem ipsum dolor sit amet consectetur adipiscing elit",
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun PreviewEmptyState() {
    TraktTheme {
        ContentEmptyList(
            contentPadding = PaddingValues(16.dp),
            filter = MediaMode.MEDIA,
        )
    }
}
