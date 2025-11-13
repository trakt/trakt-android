package tv.trakt.trakt.core.lists.sections.personal

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.model.PersonalListItem.MovieItem
import tv.trakt.trakt.core.lists.model.PersonalListItem.ShowItem
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.sheet.ListMovieContextSheet
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.sheet.ListShowContextSheet
import tv.trakt.trakt.core.lists.sections.personal.views.ListsPersonalItemView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListsPersonalView(
    viewModel: ListsPersonalViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onMoreClick: () -> Unit,
    onAllClick: (CustomList) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onShowClick: (TraktId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateMovie, state.navigateShow) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
    }

    var showContextSheet by remember { mutableStateOf<Show?>(null) }
    var movieContextSheet by remember { mutableStateOf<Movie?>(null) }

    ListsPersonalContent(
        state = state,
        list = state.list,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onShowLongClick = {
            if (!state.loading.isLoading) {
                showContextSheet = it
            }
        },
        onMovieLongClick = {
            if (!state.loading.isLoading) {
                movieContextSheet = it
            }
        },
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
        onAllClick = {
            state.list?.let {
                if (!state.items.isNullOrEmpty()) {
                    onAllClick(it)
                }
            }
        },
        onMoreClick = onMoreClick,
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
internal fun ListsPersonalContent(
    state: ListsPersonalState,
    list: CustomList?,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onMoreClick: () -> Unit = {},
    onAllClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onAllClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = spacedBy(1.dp),
                modifier = Modifier
                    .weight(1F, fill = false)
                    .fillMaxWidth(0.75F),
            ) {
                Row(
                    horizontalArrangement = spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TraktHeader(
                        title = list?.name ?: "",
                        modifier = Modifier.weight(1F, fill = false),
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_more_vertical),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .onClick { onMoreClick() }
                            .size(14.dp),
                    )
                }

                if (!list?.description.isNullOrBlank()) {
                    Text(
                        text = list.description ?: "",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(
                            fontWeight = W400,
                            lineHeight = 1.em,
                        ),
                        maxLines = 2,
                        overflow = Ellipsis,
                    )
                }
            }

            if (!state.items.isNullOrEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            translationX = (4.9).dp.toPx()
                        },
                )
            }
        }

        Spacer(modifier = Modifier.height(TraktTheme.spacing.mainRowHeaderSpace))

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                        modifier = Modifier.padding(bottom = 3.75.dp),
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
                                maxLines = 10,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }
                        state.items?.isEmpty() == true -> {
                            ContentEmptyList(
                                contentPadding = contentPadding,
                                modifier = Modifier.padding(bottom = 3.75.dp),
                            )
                        }
                        else -> {
                            ContentList(
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                                onShowLongClick = onShowLongClick,
                                onMovieLongClick = onMovieLongClick,
                                onMovieClick = onMovieClick,
                                onShowClick = onShowClick,
                            )
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
        items(count = 6) {
            VerticalMediaSkeletonCard(
                chipRatio = 0.66F,
                shimmer = false,
            )
        }
    }
}

@Composable
private fun ContentEmptyList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    LazyRow(
        userScrollEnabled = false,
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard(
                chipRatio = 0.66F,
                shimmer = false,
                containerColor = TraktTheme.colors.skeletonContainer,
            )
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<PersonalListItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
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
            ListsPersonalItemView(
                item = item,
                showMediaIcon = true,
                onMovieClick = onMovieClick,
                onShowClick = onShowClick,
                onLongClick = {
                    when (item) {
                        is ShowItem -> onShowLongClick(item.show)
                        is MovieItem -> onMovieLongClick(item.movie)
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
)
@Composable
private fun PreviewEmptyState() {
    TraktTheme {
        ContentEmptyList(
            contentPadding = PaddingValues(16.dp),
        )
    }
}
