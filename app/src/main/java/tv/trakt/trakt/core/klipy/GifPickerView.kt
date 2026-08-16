package tv.trakt.trakt.core.klipy

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.klipy.model.Gif
import tv.trakt.trakt.common.core.klipy.model.GifFormats
import tv.trakt.trakt.common.core.klipy.model.GifId
import tv.trakt.trakt.common.core.klipy.model.GifMedia
import tv.trakt.trakt.common.core.klipy.model.GifRenditions
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.core.klipy.components.GifCard
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EmptyListCard
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.theme.TraktTheme

private const val LOAD_MORE_THRESHOLD = 6

@Composable
internal fun GifPickerView(
    viewModel: GifPickerViewModel,
    modifier: Modifier = Modifier,
    onGifClick: (Gif) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GifPickerContent(
        state = state,
        onQueryChange = viewModel::updateQuery,
        onLoadMore = viewModel::loadMore,
        onGifClick = onGifClick,
        modifier = modifier,
    )
}

@Composable
private fun GifPickerContent(
    state: GifPickerState,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onGifClick: (Gif) -> Unit = {},
) {
    val inputState = rememberTextFieldState()
    val gridState = rememberLazyStaggeredGridState()

    LaunchedEffect(Unit) {
        snapshotFlow { inputState.text.toString() }
            .collect(onQueryChange)
    }

    // Both loads reset the grid; keep the user at the top of the fresh results.
    LaunchedEffect(state.query) {
        gridState.scrollToItem(0)
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= gridState.layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }

    // Keyed on the item count too: staying near the end after a page lands must request the next one.
    LaunchedEffect(shouldLoadMore, state.gifs.size) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 24.dp),
    ) {
        InputField(
            state = inputState,
            icon = painterResource(R.drawable.ic_search_off),
            placeholder = stringResource(R.string.input_placeholder_search_gifs),
            loading = state.loading.isLoading,
            containerColor = Color.Transparent,
            imeAction = ImeAction.Search,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F),
        ) {
            when {
                state.loading.isLoading && state.gifs.isEmpty() -> {
//                    FilmProgressIndicator(
//                        modifier = Modifier.size(32.dp),
//                    )
                }

                state.error != null -> {
                    EmptyListCard(text = stringResource(R.string.error_text_unexpected_error_short))
                }

                state.isEmpty -> {
                    EmptyListCard(text = stringResource(R.string.list_placeholder_gifs))
                }

                else -> {
                    val spacing = 8.dp
                    LazyVerticalStaggeredGrid(
                        state = gridState,
                        columns = StaggeredGridCells.Fixed(2),
                        horizontalArrangement = spacedBy(spacing),
                        verticalItemSpacing = spacing,
                        contentPadding = PaddingValues(
                            top = 16.dp,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(
                            items = state.gifs,
                            key = { it.id.value },
                        ) { gif ->
                            GifCard(
                                gif = gif,
                                onClick = { onGifClick(gif) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun Preview() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.DarkGray.toArgb())
    }

    TraktTheme {
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            GifPickerContent(
                state = GifPickerState(
                    gifs = previewGifs(),
                    loading = LoadingState.Done,
                ),
            )
        }
    }
}

@DevicePreview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        GifPickerContent(
            state = GifPickerState(
                query = "trakt",
                loading = LoadingState.Loading,
            ),
        )
    }
}

@DevicePreview
@Composable
private fun PreviewEmpty() {
    TraktTheme {
        GifPickerContent(
            state = GifPickerState(
                query = "no results here",
                loading = LoadingState.Done,
            ),
        )
    }
}

private fun previewGifs() =
    List(9) { index ->
        val media = GifMedia(
            url = "",
            width = 220,
            height = 140 + index * 20,
            sizeBytes = 0,
        )

        Gif(
            id = GifId(index.toLong()),
            slug = "preview-$index",
            title = "Preview GIF $index",
            tags = persistentListOf(),
            renditions = GifRenditions(
                hd = null,
                md = null,
                sm = GifFormats(gif = null, webp = media, jpg = null, mp4 = null, webm = null),
                xs = null,
            ),
            blurPreview = null,
        )
    }.toImmutableList()
