@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.CustomDate
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.dateselection.Now
import tv.trakt.trakt.ui.components.dateselection.ReleaseDate
import tv.trakt.trakt.ui.components.dateselection.UnknownDate
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.ZoneOffset.UTC

@Composable
internal fun WatchlistMovieContextView(
    item: Movie,
    viewModel: WatchlistMovieContextViewModel,
    addLocally: Boolean,
    modifier: Modifier = Modifier,
    onAddWatched: (Movie) -> Unit,
    onRemoveWatchlist: () -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf<Movie?>(null) }

    LaunchedEffect(state.loadingWatched, state.loadingWatchlist) {
        when {
            state.loadingWatched == DONE -> onAddWatched(item)
            state.loadingWatchlist == DONE -> onRemoveWatchlist()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    WatchlistMovieContextViewContent(
        movie = item,
        state = state,
        modifier = modifier,
        onAddWatched = {
            if (addLocally) {
                dateSheet = item
            } else {
                onAddWatched(item)
            }
        },
        onRemoveWatchlist = {
            confirmRemoveSheet = true
        },
    )

    @OptIn(ExperimentalMaterial3Api::class)
    ConfirmationSheet(
        active = confirmRemoveSheet,
        onYes = {
            confirmRemoveSheet = false
            viewModel.removeFromWatchlist(item.ids.trakt)
        },
        onNo = { confirmRemoveSheet = false },
        title = stringResource(R.string.button_text_watchlist),
        message = stringResource(
            R.string.warning_prompt_remove_from_watchlist,
            item.title,
        ),
    )

    DateSelectionSheet(
        movie = dateSheet,
        onDateSelected = { selectedDate ->
            viewModel.addToWatched(
                movieId = item.ids.trakt,
                customDate = selectedDate,
            )
        },
        onDismiss = {
            dateSheet = null
        },
    )
}

@Composable
private fun WatchlistMovieContextViewContent(
    movie: Movie,
    state: WatchlistMovieContextState,
    modifier: Modifier = Modifier,
    onAddWatched: () -> Unit = {},
    onRemoveWatchlist: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        PanelMediaCard(
            title = movie.title,
            titleOriginal = movie.titleOriginal,
            subtitle = remember(movie.genres) {
                movie.genres.take(2).joinToString(", ") { genre ->
                    genre.replaceFirstChar {
                        it.uppercaseChar()
                    }
                }
            },
            shadow = 4.dp,
            more = false,
            containerColor = Shade910,
            contentImageUrl = movie.images?.getPosterUrl(),
            containerImageUrl = movie.images?.getFanartUrl(THUMB),
            footerContent = {
                MovieMetaFooter(movie)
            },
        )

        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
            modifier = Modifier.padding(top = 20.dp),
        ) {
            val isLoading =
                state.loadingWatched.isLoading ||
                    state.loadingWatchlist.isLoading

            val isReleased = remember {
                movie.released?.isTodayOrBefore() ?: false
            }

            if (isReleased) {
                GhostButton(
                    enabled = !isLoading,
                    loading = state.loadingWatched.isLoading,
                    text = stringResource(R.string.button_text_mark_as_watched),
                    iconSize = 20.dp,
                    iconSpace = 16.dp,
                    onClick = onAddWatched,
                    icon = painterResource(R.drawable.ic_check),
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -6.dp.toPx()
                        },
                )
            }

            GhostButton(
                enabled = !isLoading,
                loading = state.loadingWatchlist.isLoading,
                text = stringResource(R.string.button_text_watchlist),
                onClick = onRemoveWatchlist,
                iconSize = 23.dp,
                iconSpace = 16.dp,
                icon = painterResource(R.drawable.ic_minus),
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -7.dp.toPx()
                    },
            )
        }
    }
}

@Composable
private fun DateSelectionSheet(
    movie: Movie?,
    onDateSelected: (Instant?) -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = movie != null,
        title = movie?.title.orEmpty(),
        subtitle = null,
        onResult = {
            if (movie == null) return@DateSelectionSheet
            when (it) {
                is Now -> onDateSelected(null)
                is CustomDate -> onDateSelected(it.date)
                is UnknownDate -> onDateSelected(it.date)
                is ReleaseDate -> {
                    val instantDate = movie.released?.atTime(20, 0)?.toInstant(UTC)
                    onDateSelected(instantDate)
                }
            }
        },
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            WatchlistMovieContextViewContent(
                state = WatchlistMovieContextState(),
                movie = PreviewData.movie1,
            )
        }
    }
}
