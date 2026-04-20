package tv.trakt.trakt.app.core.profile.sections.library.viewall

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.profile.ProfileConfig.LIBRARY_ALL_PAGE_LIMIT
import tv.trakt.trakt.app.core.profile.ProfileConfig.LIBRARY_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.profile.sections.library.model.LibraryItem
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun ProfileLibraryViewAllScreen(
    viewModel: ProfileLibraryViewAllViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileLibraryViewAllContent(
        state = state,
        onItemClick = {
            if (state.isLoading || state.isLoadingPage) {
                return@ProfileLibraryViewAllContent
            }
            when (it) {
                is LibraryItem.MovieItem -> onNavigateToMovie(it.movie.ids.trakt)
                is LibraryItem.EpisodeItem -> onNavigateToEpisode(it.show.ids.trakt, it.episode)
            }
        },
        onLoadNextPage = {
            viewModel.loadNextDataPage()
        },
    )
}

@Composable
private fun ProfileLibraryViewAllContent(
    state: ProfileLibraryViewAllState,
    modifier: Modifier = Modifier,
    onItemClick: (LibraryItem) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedItem by remember { mutableStateOf<LibraryItem?>(null) }
    var focusedItemKey by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            focusedItem = null
            focusedItemKey = null
            focusRequesters.clear()
        }
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
            imageUrl = when (val item = focusedItem) {
                is LibraryItem.MovieItem -> item.movie.images?.getFanartUrl(Images.Size.FULL)
                is LibraryItem.EpisodeItem -> item.show.images?.getFanartUrl(Images.Size.FULL)
                null -> null
            },
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = spacedBy(gridSpace),
            verticalArrangement = spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 16.dp,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    verticalArrangement = spacedBy(2.dp),
                    modifier = Modifier
                        .focusProperties {
                            down = focusRequesters.values.firstOrNull() ?: FocusRequester.Default
                        }
                        .focusable(),
                ) {
                    Text(
                        text = stringResource(R.string.list_title_plex_library),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading4,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!state.isLoading && state.items?.isEmpty() == true) {
                        Text(
                            text = stringResource(R.string.list_subtitle_plex_library),
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.meta.copy(fontWeight = W400),
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (state.isLoading && state.items.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        size = 42.dp,
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.items.isNullOrEmpty()) {
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].key },
                ) { index ->
                    val item = state.items[index]
                    val focusRequester = remember(item.key) {
                        focusRequesters.getOrPut(item.key) {
                            FocusRequester()
                        }
                    }

                    HorizontalMediaCard(
                        title = "",
                        containerImageUrl = remember(item.key) {
                            when (item) {
                                is LibraryItem.MovieItem -> item.movie.images?.getFanartUrl()
                                is LibraryItem.EpisodeItem -> item.show.images?.getFanartUrl()
                            }
                        },
                        onClick = { onItemClick(item) },
                        footerContent = {
                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                Text(
                                    text = remember(item.key) {
                                        when (item) {
                                            is LibraryItem.MovieItem -> item.movie.title
                                            is LibraryItem.EpisodeItem -> item.show.title
                                        }
                                    },
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                if (item is LibraryItem.EpisodeItem) {
                                    Text(
                                        text = item.episode.seasonEpisodeString(),
                                        style = TraktTheme.typography.cardSubtitle,
                                        color = TraktTheme.colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                if (item is LibraryItem.MovieItem) {
                                    val durationText = remember(item.movie.runtime) {
                                        item.movie.runtime?.inWholeMinutes?.durationFormat() ?: ""
                                    }
                                    Text(
                                        text = durationText,
                                        style = TraktTheme.typography.cardSubtitle,
                                        color = TraktTheme.colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
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
    if (size >= LIBRARY_ALL_PAGE_LIMIT && index >= size - LIBRARY_NEXT_PAGE_OFFSET) {
        onLoadNextPage()
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1000,
)
@Composable
private fun Preview() {
    TraktTheme {
        ProfileLibraryViewAllContent(
            state = ProfileLibraryViewAllState(),
            onItemClick = {},
            onLoadNextPage = {},
        )
    }
}
