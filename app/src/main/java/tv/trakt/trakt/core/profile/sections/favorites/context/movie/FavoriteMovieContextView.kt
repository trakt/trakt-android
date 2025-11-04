@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.favorites.context.movie

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
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun FavoriteMovieContextView(
    movie: Movie,
    viewModel: FavoriteMovieContextViewModel,
    modifier: Modifier = Modifier,
    onRemovedFromFavorites: () -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loading) {
        when {
            state.loading == LoadingState.DONE -> {
                onRemovedFromFavorites()
            }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    FavoriteMovieContextViewContent(
        movie = movie,
        state = state,
        modifier = modifier,
        onRemoveClick = {
            confirmRemoveSheet = true
        },
    )

    ConfirmationSheet(
        active = confirmRemoveSheet,
        onYes = {
            confirmRemoveSheet = false
            viewModel.removeFromFavorites()
        },
        onNo = { confirmRemoveSheet = false },
        title = stringResource(R.string.button_text_remove_favorites),
        message = stringResource(
            R.string.warning_prompt_remove_from_favorites,
            movie.title,
        ),
    )
}

@Composable
private fun FavoriteMovieContextViewContent(
    movie: Movie,
    state: FavoriteMovieContextState,
    modifier: Modifier = Modifier,
    onRemoveClick: () -> Unit = {},
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

        if (state.user != null) {
            MovieActionButtons(
                movie = movie,
                state = state,
                onRemoveClick = onRemoveClick,
            )
        }
    }
}

@Composable
private fun MovieActionButtons(
    movie: Movie,
    state: FavoriteMovieContextState,
    onRemoveClick: () -> Unit,
) {
    val isReleased = remember {
        movie.released?.isTodayOrBefore() ?: false
    }

    val isLoadingOrDone =
        state.loading.isLoading ||
            state.loading.isDone

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = Modifier.padding(top = 20.dp),
    ) {
        GhostButton(
            enabled = !isLoadingOrDone,
            loading = state.loading.isLoading || state.loading.isDone,
            text = stringResource(R.string.button_text_remove_favorites),
            onClick = onRemoveClick,
            icon = painterResource(R.drawable.ic_trash),
            iconSize = 22.dp,
            iconSpace = 16.dp,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -6.dp.toPx()
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
            FavoriteMovieContextViewContent(
                state = FavoriteMovieContextState(),
                movie = PreviewData.movie1,
            )
        }
    }
}
