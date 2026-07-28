package tv.trakt.trakt.app.core.home.sections.recommended.viewall

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.app.core.home.sections.recommended.model.RecommendedItem.MovieItem
import tv.trakt.trakt.app.core.home.sections.recommended.model.RecommendedItem.ShowItem
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun RecommendedViewAllScreen(
    viewModel: RecommendedViewAllViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecommendedViewAllContent(
        state = state,
        onShowClick = onNavigateToShow,
        onMovieClick = onNavigateToMovie,
    )
}

@Composable
private fun RecommendedViewAllContent(
    state: RecommendedViewAllState,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    var focusedItem by remember { mutableStateOf<RecommendedItem?>(null) }
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
            imageUrl = focusedItem?.fullFanartImage,
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
                    text = stringResource(R.string.list_title_recommended),
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
                    key = { index -> state.items[index].key },
                ) { index ->
                    val item = state.items[index]

                    val focusRequester = remember(item.key) {
                        focusRequesters.getOrPut(item.key) {
                            FocusRequester()
                        }
                    }

                    VerticalMediaCard(
                        title = item.title,
                        imageUrl = item.posterImage,
                        watched = state.collection.isWatched(item.id, item.mediaType, item.airedEpisodes),
                        watching = state.collection.isWatching(item.id, item.mediaType, item.airedEpisodes),
                        watchlist = state.collection.isWatchlist(item.id, item.mediaType),
                        onClick = {
                            when (item) {
                                is ShowItem -> onShowClick(item.show.ids.trakt)
                                is MovieItem -> onMovieClick(item.movie.ids.trakt)
                            }
                        },
                        chipContent = {
                            RecommendedCardChip(item = item)
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem = item
                                    focusedItemKey = item.key
                                }
                            },
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

@Composable
private fun RecommendedCardChip(item: RecommendedItem) {
    val subtitle = when (item) {
        is ShowItem -> {
            val episodes = stringResource(
                R.string.tag_text_number_of_episodes,
                item.show.airedEpisodes,
            )
            item.show.year?.let { "$it  •  $episodes" } ?: episodes
        }
        is MovieItem -> {
            val duration = rememberDurationFormat(item.movie.runtime?.inWholeMinutes)
            "${item.movie.yearString}  •  $duration"
        }
    }

    Column(
        verticalArrangement = spacedBy(1.dp),
    ) {
        Text(
            text = subtitle,
            style = TraktTheme.typography.cardTitle,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
