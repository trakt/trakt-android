@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.movies.sections.recommended

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MoviesRecommendedView(
    modifier: Modifier = Modifier,
    viewModel: MoviesRecommendedViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onMoreClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextSheet by remember { mutableStateOf<Movie?>(null) }

    MoviesRecommendedContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onLongClick = {
            if (!state.loading.isLoading) {
                contextSheet = it
            }
        },
        onMoreClick = {
            if (!state.loading.isLoading) {
                onMoreClick()
            }
        },
    )

    MovieContextSheet(
        movie = contextSheet,
        onDismiss = { contextSheet = null },
    )
}

@Composable
internal fun MoviesRecommendedContent(
    state: MoviesRecommendedState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onLongClick: (Movie) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.list_title_recommended),
            )
            Text(
                text = stringResource(R.string.button_text_view_all),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.buttonSecondary,
                modifier = Modifier.onClick { onMoreClick() },
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
                            items = (state.items ?: emptyList()).toImmutableList(),
                            contentPadding = contentPadding,
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
            VerticalMediaSkeletonCard(chipRatio = 0.75F)
        }
    }
}

@Composable
private fun ContentList(
    items: ImmutableList<Movie>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onLongClick: (Movie) -> Unit = {},
) {
    val currentList = remember { mutableIntStateOf(items.hashCode()) }

    LaunchedEffect(items) {
        val hashCode = items.hashCode()
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
            items = items,
            key = { it.ids.trakt.value },
        ) { item ->
            ContentListItem(
                item = item,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
                onLongClick = { onLongClick(item) },
            )
        }
    }
}

@Composable
private fun ContentListItem(
    item: Movie,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.title,
        imageUrl = item.images?.getPosterUrl(),
        onClick = onClick,
        onLongClick = onLongClick,
        chipContent = {
            Row(
                horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
            ) {
                item.released?.let {
                    InfoChip(text = it.year.toString())
                }
                item.runtime?.inWholeMinutes?.let {
                    val runtimeString = remember(item.runtime) {
                        it.durationFormat()
                    }
                    InfoChip(text = runtimeString)
                }
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
        MoviesRecommendedContent(
            state = MoviesRecommendedState(
                loading = IDLE,
            ),
        )
    }
}
