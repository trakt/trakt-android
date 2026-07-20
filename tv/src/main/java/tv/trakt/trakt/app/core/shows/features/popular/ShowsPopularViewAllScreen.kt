package tv.trakt.trakt.app.core.shows.features.popular

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
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.shows.ShowsConfig.SHOWS_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.shows.ShowsConfig.SHOWS_PAGE_LIMIT
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ShowsPopularScreen(
    viewModel: ShowsPopularViewAllViewModel,
    onNavigateToShow: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowsPopularContent(
        state = state,
        onShowClick = onNavigateToShow,
        onLoadNextPage = { viewModel.loadNextDataPage() },
    )
}

@Composable
private fun ShowsPopularContent(
    state: ShowsPopularViewAllState,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedShow by remember { mutableStateOf<Show?>(null) }
    var focusedShowId by rememberSaveable { mutableStateOf<Int?>(null) }
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    LaunchedEffect(Unit) {
        delay(500.milliseconds)
        focusRequesters[focusedShowId]?.requestSafeFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedShowId]?.requestSafeFocus()
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
                    text = stringResource(R.string.list_title_most_popular),
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

            if (state.isLoading && state.shows.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.shows.isNullOrEmpty()) {
                items(
                    count = state.shows.size,
                    key = { index -> state.shows[index].ids.trakt.value },
                ) { index ->
                    val show = state.shows[index]

                    val focusRequester = remember(show.ids.trakt.value) {
                        focusRequesters.getOrPut(show.ids.trakt.value) {
                            FocusRequester()
                        }
                    }

                    VerticalMediaCard(
                        title = show.title,
                        imageUrl = show.images?.getPosterUrl(),
                        watched = state.collection.isWatched(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watching = state.collection.isWatching(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watchlist = state.collection.isWatchlist(show.ids.trakt, MediaType.Show),
                        onClick = {
                            if (!state.isLoadingPage) {
                                onShowClick(show.ids.trakt)
                            }
                        },
                        chipContent = {
                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                val episodes = stringResource(
                                    R.string.tag_text_number_of_episodes,
                                    show.airedEpisodes,
                                )
                                Text(
                                    text = show.year?.let { "$it  •  $episodes" } ?: episodes,
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedShow = show
                                    focusedShowId = show.ids.trakt.value

                                    loadNextPageIfNeeded(
                                        size = state.shows.size,
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
    if (size >= SHOWS_PAGE_LIMIT && index >= size - SHOWS_NEXT_PAGE_OFFSET) {
        onLoadNextPage()
    }
}

@Preview
@Composable
private fun ShowsPopularContentPreview() {
    TraktTheme {
        ShowsPopularContent(
            state = ShowsPopularViewAllState(
                shows = listOf(
                    PreviewData.show1,
                ).toImmutableList(),
            ),
            onShowClick = {},
            onLoadNextPage = {},
        )
    }
}
