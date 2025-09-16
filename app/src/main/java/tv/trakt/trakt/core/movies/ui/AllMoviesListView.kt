package tv.trakt.trakt.core.movies.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.HorizontalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AllMoviesListView(
    title: @Composable () -> Unit,
    state: LazyGridState,
    items: ImmutableList<Movie>,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onTopOfList: () -> Unit = {},
    onEndOfList: () -> Unit = {},
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    val isScrolledToBottom by remember(items.size) {
        derivedStateOf {
            state.firstVisibleItemIndex >= (items.size - 10)
        }
    }

    val isScrolledToTop by remember {
        derivedStateOf {
            state.firstVisibleItemIndex == 0 &&
                state.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            onTopOfList()
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(2),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            title()
        }

        gridItems(items)

        if (loading) {
            items(count = 2) { index ->
                HorizontalMediaSkeletonCard(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }
    }
}

private fun LazyGridScope.gridItems(items: ImmutableList<Movie>) {
    items(
        items = items,
        key = { it.ids.trakt.value },
    ) { item ->
        HorizontalMediaCard(
            title = "",
            containerImageUrl = item.images?.getFanartUrl(THUMB),
            cardContent = {
                Row(
                    horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpacing),
                ) {
                    item.released?.let {
                        InfoChip(
                            text = it.year.toString(),
                            containerColor = TraktTheme.colors.chipContainerOnContent,
                        )
                    }
                    item.runtime?.inWholeMinutes?.let {
                        val runtimeString = remember(item.runtime) {
                            it.durationFormat()
                        }
                        InfoChip(
                            text = runtimeString,
                            containerColor = TraktTheme.colors.chipContainerOnContent,
                        )
                    }
                }
            },
            footerContent = {
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
                        text = remember(item.genres.size) {
                            item.genres
                                .take(2)
                                .joinToString(", ") { genre ->
                                    genre.replaceFirstChar { it.titlecase() }
                                }
                        },
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            modifier = Modifier
                .padding(bottom = TraktTheme.spacing.mainGridHorizontalSpace * 2)
                .animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
        )
    }
}
