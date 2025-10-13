package tv.trakt.trakt.core.shows.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AllShowsListView(
    state: LazyListState,
    items: ImmutableList<Show>,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    loading: Boolean = false,
    onItemClick: (Show) -> Unit = {},
    onItemLongClick: (Show) -> Unit = {},
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
            onClick = onItemClick,
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
    items: ImmutableList<Show>,
    onClick: ((Show) -> Unit)? = null,
    onLongClick: ((Show) -> Unit)? = null,
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
            onClick = onClick?.let { { onClick(item) } },
            onLongClick = onLongClick?.let { { onLongClick(item) } },
            footerContent = {
                ShowMetaFooter(show = item)
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
private fun AllShowsListViewPreview() {
    TraktTheme {
        AllShowsListView(
            state = LazyListState(),
            items = listOf(
                PreviewData.show1,
                PreviewData.show2,
            ).toImmutableList(),
        )
    }
}
