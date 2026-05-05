package tv.trakt.trakt.app.core.details.lists.details.media

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.LocalSnackbarState
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.buttons.LikeButton
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun CustomListMediaScreen(
    viewModel: CustomListMediaViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localSnack = LocalSnackbarState.current

    CustomListMediaContent(
        state = state,
        listName = viewModel.destination.listName,
        onLikeClick = {
            viewModel.setLiked(!state.like.isLiked)
        },
        onShowClick = onNavigateToShow,
        onMovieClick = onNavigateToMovie,
        onLoadNextPage = { viewModel.loadMoreData() },
    )

    LaunchedEffect(state.info) {
        state.info?.let {
            localSnack.showSnackbar(it.get(localContext))
            viewModel.clearInfo()
        }
    }
}

@Composable
private fun CustomListMediaContent(
    state: CustomListMediaState,
    listName: String,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedItem by remember { mutableStateOf<ListMediaItem?>(null) }
    var focusedItemKey by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember {
        mutableMapOf("header" to FocusRequester())
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedItemKey]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedItem?.images?.getFanartUrl(Images.Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = TraktTheme.size.verticalMediaCardSize),
            horizontalArrangement = Arrangement.spacedBy(gridSpace),
            verticalArrangement = Arrangement.spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequesters.getValue("header"))
                        .focusGroup(),
                ) {
                    Text(
                        text = listName,
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading4,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1F, false)
                            .focusProperties {
                                down = focusRequesters
                                    .entries
                                    .firstOrNull { it.key != "header" }
                                    ?.value
                                    ?: FocusRequester.Default
                            }
                            .focusable(),
                    )

                    LikeButton(
                        text = rememberThousandsFormat(state.like.likesCount),
                        liked = state.like.isLiked,
                        loading = state.like.isLoading,
                        enabled = !state.like.isLoading,
                        onClick = onLikeClick,
                    )
                }
            }

            if (state.isLoading && state.items.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.items.isNullOrEmpty()) {
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].key },
                ) { index ->
                    val item = state.items[index]
                    val focusRequester = focusRequesters.getOrPut(item.key) {
                        FocusRequester()
                    }

                    VerticalMediaCard(
                        title = item.title,
                        imageUrl = item.images?.getPosterUrl(),
                        onClick = {
                            if (!state.isLoadingPage) {
                                when (item) {
                                    is ListMediaItem.ShowItem -> onShowClick(item.id)
                                    is ListMediaItem.MovieItem -> onMovieClick(item.id)
                                }
                            }
                        },
                        chipContent = {
                            when (item) {
                                is ListMediaItem.ShowItem -> {
                                    val episodes = item.show.airedEpisodes
                                    if (episodes > 0) {
                                        InfoChip(
                                            text = stringResource(R.string.tag_text_number_of_episodes, episodes),
                                        )
                                    }
                                }

                                is ListMediaItem.MovieItem -> {
                                    val runtime = item.movie.runtime?.inWholeMinutes
                                    if (runtime != null) {
                                        InfoChip(
                                            text = rememberDurationFormat(runtime),
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem = item
                                    focusedItemKey = item.key

                                    loadNextPageIfNeeded(
                                        size = state.items.size,
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
    if (size >= CUSTOM_LIST_PAGE_LIMIT && index >= size - CUSTOM_LIST_NEXT_PAGE_OFFSET) {
        onLoadNextPage()
    }
}

@Preview(
    name = "720P",
    device = "id:tv_720p",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Preview(
    name = "4K",
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        CustomListMediaContent(
            listName = "Custom List",
            state = CustomListMediaState(
                items = (
                    (1..10).map {
                        ListMediaItem.ShowItem(
                            PreviewData.show1.copy(ids = Ids(TraktId(it), SlugId(it.toString()))),
                        )
                    } + (11..20).map {
                        ListMediaItem.MovieItem(
                            PreviewData.movie1.copy(ids = Ids(TraktId(it), SlugId(it.toString()))),
                        )
                    }
                ).toImmutableList(),
            ),
            onShowClick = {},
            onMovieClick = {},
            onLikeClick = {},
            onLoadNextPage = {},
        )
    }
}
