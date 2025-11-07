@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.discover.sections.trending

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverItem.MovieItem
import tv.trakt.trakt.core.discover.model.DiscoverItem.ShowItem
import tv.trakt.trakt.core.discover.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DiscoverTrendingView(
    modifier: Modifier = Modifier,
    viewModel: DiscoverTrendingViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onShowClick: (TraktId) -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextShowSheet by remember { mutableStateOf<Show?>(null) }
    var contextMovieSheet by remember { mutableStateOf<Movie?>(null) }

    DiscoverTrendingContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onMoreClick = {
            if (!state.loading.isLoading) {
                onMoreClick()
            }
        },
        onClick = {
            when (it) {
                is ShowItem -> onShowClick(it.id)
                is MovieItem -> onMovieClick(it.id)
            }
        },
        onLongClick = {
            if (state.loading.isLoading) {
                return@DiscoverTrendingContent
            }
            when (it) {
                is ShowItem -> contextShowSheet = it.show
                is MovieItem -> contextMovieSheet = it.movie
            }
        },
    )

    ShowContextSheet(
        show = contextShowSheet,
        onDismiss = { contextShowSheet = null },
    )

    MovieContextSheet(
        movie = contextMovieSheet,
        onDismiss = { contextMovieSheet = null },
    )
}

@Composable
internal fun DiscoverTrendingContent(
    state: DiscoverTrendingState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: (DiscoverItem) -> Unit = {},
    onLongClick: (DiscoverItem) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onMoreClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.list_title_trending),
            )
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
                    if (state.error != null) {
                        Text(
                            text = "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.meta,
                            maxLines = 10,
                            modifier = Modifier.padding(contentPadding),
                        )
                    } else {
                        ContentList(
                            mode = state.mode,
                            listItems = (state.items ?: emptyList()).toImmutableList(),
                            contentPadding = contentPadding,
                            onClick = onClick,
                            onLongClick = onLongClick,
                        )
                    }
                }
            }
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
            VerticalMediaSkeletonCard(
                chipRatio = 0.5F,
            )
        }
    }
}

@Composable
private fun ContentList(
    mode: MediaMode?,
    listState: LazyListState = rememberLazyListState(),
    listItems: ImmutableList<DiscoverItem>,
    contentPadding: PaddingValues,
    onClick: (DiscoverItem) -> Unit,
    onLongClick: (DiscoverItem) -> Unit,
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
            ContentListItem(
                item = item,
                mode = mode,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
                onClick = { onClick(item) },
                onLongClick = { onLongClick(item) },
            )
        }
    }
}

@Composable
private fun ContentListItem(
    item: DiscoverItem,
    mode: MediaMode?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.title,
        imageUrl = item.images?.getPosterUrl(),
        onClick = onClick,
        onLongClick = onLongClick,
        chipContent = { modifier ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = modifier,
            ) {
                if (mode == MEDIA) {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = null,
                        tint = TraktTheme.colors.chipContent,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = "•",
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_person_double),
                    contentDescription = null,
                    tint = TraktTheme.colors.chipContent,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = item.count.thousandsFormat(),
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }
        },
        modifier = modifier,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        DiscoverTrendingContent(
            state = DiscoverTrendingState(
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
        DiscoverTrendingContent(
            state = DiscoverTrendingState(
                loading = LOADING,
            ),
        )
    }
}
