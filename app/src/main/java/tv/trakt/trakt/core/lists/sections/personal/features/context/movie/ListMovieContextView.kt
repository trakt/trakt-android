@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.personal.features.context.movie

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
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListMovieContextView(
    movie: Movie,
    list: CustomList,
    viewModel: ListMovieContextViewModel,
    modifier: Modifier = Modifier,
    onAddWatched: (Movie) -> Unit,
    onAddWatchlist: (Movie) -> Unit,
    onRemoveWatched: (Movie) -> Unit,
    onRemoveWatchlist: (Movie) -> Unit,
    onRemoveList: (Movie) -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveListSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }

    LaunchedEffect(
        state.loadingWatched,
        state.loadingWatchlist,
        state.loadingList,
    ) {
        when {
            state.loadingWatched == LoadingState.DONE -> when {
                !state.isWatched || state.isWatchlist -> onAddWatched(movie)
                else -> onRemoveWatched(movie)
            }
            state.loadingWatchlist == LoadingState.DONE -> when {
                !state.isWatchlist -> onAddWatchlist(movie)
                else -> onRemoveWatchlist(movie)
            }
            state.loadingList == LoadingState.DONE -> {
                onRemoveList(movie)
            }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    ListMovieContextViewContent(
        movie = movie,
        state = state,
        modifier = modifier,
        onWatchedClick = {
            when {
                state.isWatched && !state.isWatchlist -> confirmRemoveWatchedSheet = true
                else -> viewModel.addToWatched()
            }
        },
        onWatchlistClick = {
            when {
                state.isWatchlist -> confirmRemoveWatchlistSheet = true
                else -> viewModel.addToWatchlist()
            }
        },
        onRemoveListClick = {
            confirmRemoveListSheet = true
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

    ConfirmationSheet(
        active = confirmRemoveListSheet,
        onYes = {
            confirmRemoveListSheet = false
            viewModel.removeFromList()
        },
        onNo = { confirmRemoveListSheet = false },
        title = stringResource(R.string.button_text_remove_from_list),
        message = stringResource(
            R.string.warning_prompt_remove_from_personal_list,
            movie.title,
            list.name,
        ),
    )
}

@Composable
private fun ListMovieContextViewContent(
    movie: Movie,
    state: ListMovieContextState,
    modifier: Modifier = Modifier,
    onWatchedClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = {},
    onRemoveListClick: () -> Unit = {},
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
            containerColor = Shade910,
            contentImageUrl = movie.images?.getPosterUrl(),
            containerImageUrl = movie.images?.getFanartUrl(THUMB),
            footerContent = {
                MovieMetaFooter(movie)
            },
        )

        ActionButtons(
            movie = movie,
            state = state,
            onWatchedClick = onWatchedClick,
            onWatchlistClick = onWatchlistClick,
            onRemoveListClick = onRemoveListClick,
        )
    }
}

@Composable
private fun ActionButtons(
    movie: Movie,
    state: ListMovieContextState,
    onWatchedClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onRemoveListClick: () -> Unit,
) {
    val isReleased = remember {
        movie.released?.isTodayOrBefore() ?: false
    }

    val isLoadingOrDone =
        state.loadingWatched.isLoading ||
            state.loadingWatchlist.isLoading ||
            state.loadingWatchlist.isDone ||
            state.loadingWatched.isDone ||
            state.loadingList.isLoading ||
            state.loadingList.isDone

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = Modifier.padding(top = 24.dp),
    ) {
        if (isReleased) {
            if (state.isWatched && !state.isWatchlist) {
                GhostButton(
                    enabled = !isLoadingOrDone,
                    loading = state.loadingWatched.isLoading || state.loadingWatched.isDone,
                    text = stringResource(R.string.button_text_remove_from_history),
                    onClick = onWatchedClick,
                    icon = painterResource(R.drawable.ic_trash),
                    iconSize = 22.dp,
                    iconSpace = 16.dp,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -3.dp.toPx()
                        },
                )
            } else {
                GhostButton(
                    enabled = !isLoadingOrDone,
                    loading = state.loadingWatched.isLoading || state.loadingWatched.isDone,
                    text = stringResource(R.string.button_text_mark_as_watched),
                    iconSize = 22.dp,
                    iconSpace = 16.dp,
                    onClick = onWatchedClick,
                    icon = painterResource(R.drawable.ic_check_round),
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -3.dp.toPx()
                        },
                )
            }
        }

        GhostButton(
            enabled = !isLoadingOrDone,
            loading = state.loadingList.isLoading,
            text = stringResource(R.string.button_text_remove_from_list),
            onClick = onRemoveListClick,
            iconSize = 22.dp,
            iconSpace = 17.dp,
            icon = painterResource(R.drawable.ic_minus),
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
            iconSpace = 17.dp,
            icon = when {
                state.isWatchlist -> painterResource(R.drawable.ic_minus)
                else -> painterResource(R.drawable.ic_plus)
            },
            modifier = Modifier
                .graphicsLayer {
                    translationX = -3.dp.toPx()
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
            ListMovieContextViewContent(
                state = ListMovieContextState(),
                movie = PreviewData.movie1,
            )
        }
    }
}
