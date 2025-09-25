package tv.trakt.trakt.core.movies.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.White
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AllMoviesListView(
    state: LazyListState,
    items: ImmutableList<Movie>,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    loading: Boolean = false,
    onItemLongClick: (Movie) -> Unit = {},
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
            state.firstVisibleItemIndex >= (items.size - 5)
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

    LazyColumn(
        state = state,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        if (title != null) {
            item { title() }
        }

        listItems(
            items = items,
            onLongClick = onItemLongClick,
        )

        if (loading) {
            item {
                FilmProgressIndicator(
                    size = 32.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun LazyListScope.listItems(
    items: ImmutableList<Movie>,
    onLongClick: ((Movie) -> Unit)? = null,
) {
    items(
        items = items,
        key = { it.ids.trakt.value },
    ) { item ->
        val genresText = remember(item.genres) {
            item.genres.take(2).joinToString(", ") { genre ->
                genre.replaceFirstChar {
                    it.uppercaseChar()
                }
            }
        }

        PanelMediaCard(
            title = item.title,
            titleOriginal = item.titleOriginal,
            subtitle = genresText,
            contentImageUrl = item.images?.getPosterUrl(),
            containerImageUrl = item.images?.getFanartUrl(THUMB),
            onLongClick = onLongClick?.let { { onLongClick(item) } },
            footerContent = {
                Row(
                    horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val metaString = remember {
                        val separator = "  •  "
                        buildString {
                            item.released?.let {
                                append(it.year)
                            }
                            item.runtime?.let {
                                if (isNotEmpty()) append(separator)
                                append(it.inWholeMinutes.durationFormat())
                            }
                            if (!item.certification.isNullOrBlank()) {
                                if (isNotEmpty()) append(separator)
                                append(item.certification)
                            }
                        }
                    }
                    Text(
                        text = metaString,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(4.dp),
                    ) {
                        val grayFilter = remember {
                            ColorFilter.colorMatrix(
                                ColorMatrix().apply {
                                    setToSaturation(0F)
                                },
                            )
                        }
                        val whiteFilter = remember {
                            ColorFilter.tint(White)
                        }

                        Spacer(modifier = Modifier.weight(1F))

                        Image(
                            painter = painterResource(R.drawable.ic_trakt_icon),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            colorFilter = if (item.rating.rating > 0) whiteFilter else grayFilter,
                        )
                        Text(
                            text = if (item.rating.rating > 0) "${item.rating.ratingPercent}%" else "-",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                        )
                    }
                }
            },
            modifier = Modifier
                .padding(bottom = TraktTheme.spacing.mainListVerticalSpace),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun AllMoviesListViewPreview() {
    TraktTheme {
        AllMoviesListView(
            state = LazyListState(),
            items = listOf(
                PreviewData.movie1,
                PreviewData.movie2,
            ).toImmutableList(),
        )
    }
}
