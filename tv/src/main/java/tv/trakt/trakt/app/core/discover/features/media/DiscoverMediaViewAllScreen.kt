package tv.trakt.trakt.app.core.discover.features.media

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.discover.DiscoverConfig.DISCOVER_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.discover.DiscoverConfig.DISCOVER_PAGE_LIMIT
import tv.trakt.trakt.app.core.discover.views.DiscoverItemCard
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun DiscoverMediaViewAllScreen(
    viewModel: DiscoverMediaViewAllViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DiscoverMediaViewAllContent(
        state = state,
        onItemClick = { item ->
            when (item) {
                is DiscoverItem.ShowItem -> onNavigateToShow(item.id)
                is DiscoverItem.MovieItem -> onNavigateToMovie(item.id)
            }
        },
        onLoadNextPage = viewModel::loadNextDataPage,
    )
}

@Composable
private fun DiscoverMediaViewAllContent(
    state: DiscoverMediaViewAllState,
    modifier: Modifier = Modifier,
    onItemClick: (DiscoverItem) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedImages by remember { mutableStateOf<Images?>(null) }
    var focusedItemKey by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(Unit) {
        delay(250.milliseconds)
        focusRequesters[focusedItemKey]?.requestSafeFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedItemKey]?.requestSafeFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedImages?.getFanartUrl(Images.Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

        val items = state.items
        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = TraktTheme.size.verticalMediaCardSize),
            horizontalArrangement = spacedBy(gridSpace),
            verticalArrangement = spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(state.section.titleRes),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .focusProperties {
                            down = focusRequesters.values.firstOrNull() ?: FocusRequester.Default
                        }
                        .focusable(),
                )
            }

            if (state.isLoading && items.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!items.isNullOrEmpty()) {
                items(
                    count = items.size,
                    key = { index -> items[index].key },
                ) { index ->
                    val item = items[index]
                    val focusRequester = remember(item.key) {
                        focusRequesters.getOrPut(item.key) { FocusRequester() }
                    }

                    DiscoverItemCard(
                        item = item,
                        section = state.section,
                        collection = state.collection,
                        onClick = {
                            if (!state.isLoadingPage) {
                                onItemClick(item)
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedImages = item.images
                                    focusedItemKey = item.key

                                    loadNextPageIfNeeded(
                                        size = items.size,
                                        index = index,
                                        onLoadNextPage = onLoadNextPage,
                                    )
                                }
                            },
                    )
                }
            }

            if (state.isLoadingPage) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            }
        }
    }

    if (state.error != null) {
        GenericErrorView(
            error = state.error,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
        )
    }
}

private fun loadNextPageIfNeeded(
    size: Int,
    index: Int,
    onLoadNextPage: () -> Unit,
) {
    if (size >= DISCOVER_PAGE_LIMIT && index >= size - DISCOVER_NEXT_PAGE_OFFSET) {
        onLoadNextPage()
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        DiscoverMediaViewAllContent(
            state = DiscoverMediaViewAllState(
                items = listOf(
                    DiscoverItem.MovieItem(count = 1234, movie = PreviewData.movie1),
                ).toImmutableList(),
            ),
            onItemClick = {},
            onLoadNextPage = {},
        )
    }
}
