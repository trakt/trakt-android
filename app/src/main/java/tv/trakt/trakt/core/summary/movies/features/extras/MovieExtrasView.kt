@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.movies.features.extras

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
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
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.FilterChipSkeleton
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.HorizontalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieExtrasView(
    viewModel: MovieExtrasViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieExtrasContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onClick = { uriHandler.openUri(it.url) },
        onFilterClick = { viewModel.toggleFilter(it) },
    )
}

@Composable
private fun MovieExtrasContent(
    state: MovieExtrasState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: ((ExtraVideo) -> Unit)? = null,
    onFilterClick: ((String) -> Unit)? = null,
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
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.list_title_extras),
            )
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoading(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    Column(
                        verticalArrangement = spacedBy(0.dp),
                    ) {
                        if (state.filters.filters.size > 1) {
                            ContentFilters(
                                state = state.filters,
                                onFilterClick = onFilterClick ?: {},
                            )
                        }

                        if (state.items?.isEmpty() == true) {
                            ContentEmpty(
                                contentPadding = headerPadding,
                            )
                        } else {
                            ContentList(
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                                onClick = onClick,
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
    listItems: ImmutableList<ExtraVideo>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onClick: ((ExtraVideo) -> Unit)? = null,
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
            key = { it.url },
        ) { item ->
            HorizontalMediaCard(
                title = "",
                more = false,
                containerImageUrl = item.getYoutubeImageUrl,
                onClick = { onClick?.invoke(item) },
                footerContent = {
                    Column(
                        verticalArrangement = Arrangement.Absolute.spacedBy(1.dp),
                    ) {
                        Text(
                            text = item.title,
                            style = TraktTheme.typography.cardTitle,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = Ellipsis,
                        )

                        Text(
                            text = item.type.replaceFirstChar { it.uppercaseChar() },
                            style = TraktTheme.typography.cardSubtitle,
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = Ellipsis,
                        )
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

@Composable
private fun ContentFilters(
    state: MovieExtrasState.FiltersState,
    onFilterClick: (String) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
        ),
        paddingVertical = PaddingValues(
            bottom = 18.dp,
        ),
    ) {
        for (filter in state.filters) {
            FilterChip(
                text = filter
                    .replaceFirstChar {
                        it.uppercaseChar()
                    },
                selected = state.selectedFilter == filter,
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun ContentLoading(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
    ) {
        LazyRow(
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = contentPadding,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
                .alpha(if (visible) 1F else 0F),
        ) {
            items(count = 10) {
                FilterChipSkeleton()
            }
        }

        LazyRow(
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = contentPadding,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (visible) 1F else 0F),
        ) {
            items(count = 3) {
                HorizontalMediaSkeletonCard(
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
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
            MovieExtrasContent(
                state = MovieExtrasState(),
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
            MovieExtrasContent(
                state = MovieExtrasState(
                    items = emptyList<ExtraVideo>().toImmutableList(),
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
            MovieExtrasContent(
                state = MovieExtrasState(
                    items = emptyList<ExtraVideo>().toImmutableList(),
                    loading = DONE,
                ),
            )
        }
    }
}
