@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.movies.ui.context

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieContextView(
    movie: Movie,
    viewModel: MovieContextViewModel,
    modifier: Modifier = Modifier,
    onAddWatched: (Movie) -> Unit,
    onAddWatchlist: (Movie) -> Unit,
    onRemoveWatched: (Movie) -> Unit,
    onRemoveWatchlist: (Movie) -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingWatched, state.loadingWatchlist) {
        when {
            state.loadingWatched == LoadingState.DONE -> when {
                state.isWatched -> onAddWatched(movie)
                else -> onRemoveWatched(movie)
            }
            state.loadingWatchlist == LoadingState.DONE -> when {
                state.isWatchlist -> onAddWatchlist(movie)
                else -> onRemoveWatchlist(movie)
            }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    MovieContextViewContent(
        movie = movie,
        state = state,
        modifier = modifier,
        onWatchedClick = {
            if (state.isWatched) {
                confirmRemoveWatchedSheet = true
            } else {
                viewModel.addToWatched()
            }
        },
        onWatchlistClick = {
            if (state.isWatchlist) {
                confirmRemoveWatchlistSheet = true
            } else {
                viewModel.addToWatchlist()
            }
        },
    )

    ConfirmationSheet(
        active = confirmRemoveWatchlistSheet,
        onYes = {
            confirmRemoveWatchlistSheet = false
            viewModel.removeFromWatchlist()
        },
        onNo = { confirmRemoveWatchlistSheet = false },
        title = stringResource(R.string.button_text_watchlist),
        message = stringResource(
            R.string.warning_prompt_remove_from_watchlist,
            movie.title,
        ),
    )

    ConfirmationSheet(
        active = confirmRemoveWatchedSheet,
        onYes = {
            confirmRemoveWatchedSheet = false
            viewModel.removeFromWatched()
        },
        onNo = { confirmRemoveWatchedSheet = false },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            movie.title,
        ),
    )
}

@Composable
private fun MovieContextViewContent(
    movie: Movie,
    state: MovieContextState,
    modifier: Modifier = Modifier,
    onWatchedClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        val genresText = remember(movie.genres) {
            movie.genres.take(2).joinToString(", ") { genre ->
                genre.replaceFirstChar {
                    it.uppercaseChar()
                }
            }
        }

        PanelMediaCard(
            title = movie.title,
            titleOriginal = movie.titleOriginal,
            subtitle = genresText,
            shadow = 4.dp,
            containerColor = Shade910,
            contentImageUrl = movie.images?.getPosterUrl(),
            containerImageUrl = movie.images?.getFanartUrl(THUMB),
            footerContent = {
                Row(
                    horizontalArrangement = Arrangement.Absolute.spacedBy(TraktTheme.spacing.chipsSpace),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!movie.certification.isNullOrBlank()) {
                        InfoChip(
                            text = movie.certification ?: "",
                        )
                    }
                    movie.released?.let {
                        InfoChip(
                            text = it.year.toString(),
                        )
                    }
                    movie.runtime?.let {
                        InfoChip(
                            text = it.inWholeMinutes.durationFormat(),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.spacedBy(3.dp),
                    ) {
                        val grayFilter = remember {
                            ColorFilter.colorMatrix(
                                ColorMatrix().apply {
                                    setToSaturation(0F)
                                },
                            )
                        }
                        val redFilter = remember {
                            ColorFilter.tint(Red500)
                        }

                        Spacer(modifier = Modifier.weight(1F))

                        Image(
                            painter = painterResource(R.drawable.ic_heart),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            colorFilter = if (movie.rating.rating > 0) redFilter else grayFilter,
                        )
                        Text(
                            text = if (movie.rating.rating > 0) "${movie.rating.ratingPercent}%" else "-",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.meta,
                        )
                    }
                }
            },
        )

        MovieActionButtons(
            movie = movie,
            state = state,
            onWatchedClick = onWatchedClick,
            onWatchlistClick = onWatchlistClick,
        )
    }
}

@Composable
private fun MovieActionButtons(
    movie: Movie,
    state: MovieContextState,
    onWatchedClick: () -> Unit,
    onWatchlistClick: () -> Unit,
) {
    val isReleased = remember {
        movie.released?.isTodayOrBefore() ?: false
    }

    val isLoadingOrDone =
        state.loadingWatched.isLoading ||
            state.loadingWatchlist.isLoading ||
            state.loadingWatchlist.isDone ||
            state.loadingWatched.isDone

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = Modifier
            .padding(top = 24.dp),
    ) {
        if (isReleased) {
            GhostButton(
                enabled = !isLoadingOrDone,
                loading = state.loadingWatched.isLoading || state.loadingWatched.isDone,
                text = when {
                    state.isWatched -> stringResource(R.string.button_text_remove_from_history)
                    else -> stringResource(R.string.button_text_mark_as_watched)
                },
                iconSize = 20.dp,
                iconSpace = 16.dp,
                onClick = onWatchedClick,
                icon = when {
                    state.isWatched -> painterResource(R.drawable.ic_trash)
                    else -> painterResource(R.drawable.ic_check_round)
                },
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -3.dp.toPx()
                    },
            )
        }

        GhostButton(
            enabled = !isLoadingOrDone,
            loading = state.loadingWatchlist.isLoading || state.loadingWatchlist.isDone,
            text = stringResource(R.string.button_text_watchlist),
            onClick = onWatchlistClick,
            iconSize = 22.dp,
            iconSpace = 16.dp,
            icon = when {
                state.isWatchlist -> painterResource(R.drawable.ic_minus)
                else -> painterResource(R.drawable.ic_plus)
            },
            modifier = Modifier
                .graphicsLayer {
                    translationX = -5.dp.toPx()
                },
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieContextViewContent(
                state = MovieContextState(),
                movie = PreviewData.movie1,
            )
        }
    }
}
