package tv.trakt.trakt.core.movies.ui.context

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieContextView(
    movie: Movie,
    viewModel: MovieContextViewModel,
    modifier: Modifier = Modifier,
    onAddWatchlist: (Movie) -> Unit,
    onRemoveWatchlist: (Movie) -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingWatched, state.loadingWatchlist) {
        when {
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
        item = movie,
        state = state,
        modifier = modifier,
        onWatchlistClick = {
            if (state.isWatchlist) {
                confirmRemoveWatchlistSheet = true
            } else {
                viewModel.addToWatchlist()
            }
        },
    )

    @OptIn(ExperimentalMaterial3Api::class)
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
}

@Composable
private fun MovieContextViewContent(
    item: Movie,
    state: MovieContextState,
    modifier: Modifier = Modifier,
    onWatchlistClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
        ) {
            VerticalMediaCard(
                title = "",
                corner = 12.dp,
                width = TraktTheme.size.verticalSmallMediaCardSize,
                imageUrl = item.images?.getPosterUrl(),
                modifier = Modifier.shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                ),
            )

            Column(
                verticalArrangement = spacedBy(1.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.title,
                    style = TraktTheme.typography.cardTitle.copy(fontSize = 13.sp),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = stringResource(R.string.translated_value_type_movie),
                    style = TraktTheme.typography.cardSubtitle.copy(fontSize = 13.sp),
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .padding(top = 24.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Shade910),
        )

        MovieActionButtons(
            state = state,
            onWatchlistClick = onWatchlistClick,
        )
    }
}

@Composable
private fun MovieActionButtons(
    state: MovieContextState,
    onWatchlistClick: () -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = Modifier
            .padding(top = 10.dp),
    ) {
        val isLoadingOrDone =
            state.loadingWatched.isLoading ||
                state.loadingWatchlist.isLoading ||
                state.loadingWatchlist.isDone ||
                state.loadingWatched.isDone

        GhostButton(
            enabled = !isLoadingOrDone,
            loading = state.loadingWatched.isLoading || state.loadingWatched.isDone,
            text = when {
                state.isWatched -> stringResource(R.string.button_text_remove_from_history)
                else -> stringResource(R.string.button_text_mark_as_watched)
            },
            iconSize = 20.dp,
            iconSpace = 16.dp,
            onClick = {},
            icon = when {
                state.isWatched -> painterResource(R.drawable.ic_trash)
                else -> painterResource(R.drawable.ic_check_round)
            },
            modifier = Modifier
                .graphicsLayer {
                    translationX = -3.dp.toPx()
                },
        )

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
    backgroundColor = 0xFF131517,
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
                item = PreviewData.movie1,
            )
        }
    }
}
