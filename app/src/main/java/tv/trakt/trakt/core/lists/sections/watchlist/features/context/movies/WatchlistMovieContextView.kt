@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)

package tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun WatchlistMovieContextView(
    item: Movie,
    watched: Boolean,
    addLocally: Boolean,
    viewModel: WatchlistMovieContextViewModel,
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
        watched = watched,
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
    RemoveConfirmationSheet(
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
    watched: Boolean,
    state: WatchlistMovieContextState,
    modifier: Modifier = Modifier,
    onAddWatched: () -> Unit = {},
    onRemoveWatchlist: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = movie.title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
                maxLines = 1,
                overflow = Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = TraktTheme.typography.heading3.fontSize,
                    minFontSize = 20.sp,
                    stepSize = 2.sp,
                ),
            )

            MovieMetaFooter(
                movie = movie,
                secondary = true,
                textStyle = TraktTheme.typography.paragraphSmaller,
            )
        }

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        if (state.user != null) {
            MovieActionButtons(
                movie = movie,
                watched = watched,
                onWatchedClick = onAddWatched,
                onWatchlistClick = onRemoveWatchlist,
                state = state,
                modifier = Modifier
                    .padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun MovieActionButtons(
    modifier: Modifier = Modifier,
    movie: Movie,
    watched: Boolean,
    onWatchedClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    state: WatchlistMovieContextState,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier,
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
                text = stringResource(
                    when {
                        watched -> R.string.button_text_watch_again
                        else -> R.string.button_text_mark_as_watched
                    },
                ),
                iconSize = 20.dp,
                iconSpace = 16.dp,
                onClick = onWatchedClick,
                icon = painterResource(
                    when {
                        watched -> R.drawable.ic_check_double
                        else -> R.drawable.ic_check
                    },
                ),
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
            onClick = onWatchlistClick,
            iconSize = 24.dp,
            iconSpace = 16.dp,
            icon = painterResource(R.drawable.ic_minus),
            modifier = Modifier
                .graphicsLayer {
                    translationX = -8.5.dp.toPx()
                },
        )
    }
}

@Composable
private fun DateSelectionSheet(
    movie: Movie?,
    onDateSelected: (DateSelectionResult?) -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = movie != null,
        title = movie?.title.orEmpty(),
        subtitle = null,
        onResult = {
            if (movie == null) return@DateSelectionSheet
            onDateSelected(it)
        },
        onDismiss = onDismiss,
    )
}

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
                state = WatchlistMovieContextState(
                    user = PreviewData.user1,
                ),
                movie = PreviewData.movie1,
                watched = false,
            )
        }
    }
}
