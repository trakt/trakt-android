package tv.trakt.trakt.app.core.profile.sections.favorites.shows.viewall

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
import androidx.compose.ui.Alignment
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
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.profile.ProfileConfig.FAVORITES_ALL_PAGE_LIMIT
import tv.trakt.trakt.app.core.profile.ProfileConfig.FAVORITES_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun ProfileFavoriteShowsViewAllScreen(
    viewModel: ProfileFavoriteShowsViewAllViewModel,
    onNavigateToShow: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileFavoriteShowsViewAllContent(
        state = state,
        onShowClick = {
            if (state.isLoading || state.isLoadingPage) {
                return@ProfileFavoriteShowsViewAllContent
            }
            onNavigateToShow(it.ids.trakt)
        },
        onLoadNextPage = {
            viewModel.loadNextDataPage()
        },
    )
}

@Composable
private fun ProfileFavoriteShowsViewAllContent(
    state: ProfileFavoriteShowsViewAllState,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedShow by remember { mutableStateOf<Show?>(null) }
    var focusedShowId by rememberSaveable { mutableStateOf<Int?>(null) }
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    LaunchedEffect(state.isLoading) {
        // Used when list is updated after user comes back and modifies history/watchlist etc.
        if (state.isLoading) {
            focusedShow = null
            focusedShowId = null
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
                    focusRequesters[focusedShowId]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedShow?.images?.getFanartUrl(Images.Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

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
                    text = stringResource(R.string.list_title_favorite_shows),
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

            if (state.isLoading && state.items.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.items.isNullOrEmpty()) {
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].ids.trakt.value },
                ) { index ->
                    val show = state.items[index]
                    val focusRequester = focusRequesters.getOrPut(show.ids.trakt.value) {
                        FocusRequester()
                    }

                    VerticalMediaCard(
                        title = show.title,
                        imageUrl = show.images?.getPosterUrl(),
                        onClick = {
                            if (!state.isLoadingPage) {
                                onShowClick(show)
                            }
                        },
                        chipContent = {
                            InfoChip(
                                text = stringResource(R.string.tag_text_number_of_episodes, show.airedEpisodes),
                            )
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedShow = show
                                    focusedShowId = show.ids.trakt.value

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
    if (size >= FAVORITES_ALL_PAGE_LIMIT && index >= size - FAVORITES_NEXT_PAGE_OFFSET) {
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
        ProfileFavoriteShowsViewAllContent(
            state = ProfileFavoriteShowsViewAllState(),
            onShowClick = {},
            onLoadNextPage = {},
        )
    }
}
