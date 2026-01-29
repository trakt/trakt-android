@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.movies.features.related

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieRelatedView(
    viewModel: MovieRelatedViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onClick: ((Movie) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextSheet by remember { mutableStateOf<Movie?>(null) }

    MovieRelatedContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onClick = onClick,
        onLongClick = { contextSheet = it },
    )

    MovieContextSheet(
        movie = contextSheet,
        onDismiss = { contextSheet = null },
    )
}

@Composable
private fun MovieRelatedContent(
    state: MovieRelatedState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (collapsed: Boolean) -> Unit = {},
    onClick: ((Movie) -> Unit)? = null,
    onLongClick: ((Movie) -> Unit)? = null,
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
            title = stringResource(R.string.list_title_related_movies),
            chevron = false,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
        )

        if (state.collapsed != true) {
            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
            ) { loading ->
                when (loading) {
                    IDLE, LOADING -> {
                        ContentLoading(
                            visible = loading.isLoading,
                            contentPadding = contentPadding,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }

                    DONE -> {
                        if (state.items?.isEmpty() == true) {
                            ContentEmpty(
                                contentPadding = headerPadding,
                            )
                        } else {
                            ContentList(
                                listItems = state.items ?: EmptyImmutableList,
                                collection = state.collection,
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
}

@Composable
private fun ContentList(
    listItems: ImmutableList<Movie>,
    collection: UserCollectionState,
    contentPadding: PaddingValues,
    onClick: ((Movie) -> Unit)? = null,
    onLongClick: ((Movie) -> Unit)? = null,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.ids.trakt.value },
        ) { item ->
            VerticalMediaCard(
                title = item.title,
                imageUrl = item.images?.getPosterUrl(),
                watched = collection.isWatched(item.ids.trakt, MediaType.MOVIE),
                watchlist = collection.isWatchlist(item.ids.trakt, MediaType.MOVIE),
                onClick = { onClick?.invoke(item) },
                onLongClick = { onLongClick?.invoke(item) },
                chipSpacing = 10.dp,
                chipContent = { chipModifier ->
                    val footerText = remember {
                        val runtime = item.runtime?.inWholeMinutes
                        if (runtime != null) {
                            "${item.released?.year ?: item.year} • ${runtime.durationFormat()}"
                        } else {
                            "${item.released?.year ?: item.year}"
                        }
                    }
                    Text(
                        text = footerText,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = chipModifier,
                    )
                },
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
        }
    }
}

@Composable
private fun ContentLoading(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 12) {
            VerticalMediaSkeletonCard(
                chipSpacing = 8.dp,
            )
        }
    }
}

@Composable
private fun ContentEmpty(contentPadding: PaddingValues) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = Modifier.padding(contentPadding),
    )
}

// -- Previews --

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieRelatedContent(
                state = MovieRelatedState(),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieRelatedContent(
                state = MovieRelatedState(
                    items = emptyList<Movie>().toImmutableList(),
                    loading = LOADING,
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieRelatedContent(
                state = MovieRelatedState(
                    items = emptyList<Movie>().toImmutableList(),
                    loading = DONE,
                ),
            )
        }
    }
}
