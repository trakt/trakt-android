@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.favorites.context.show

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
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun FavoriteShowContextView(
    show: Show,
    viewModel: FavoriteShowContextViewModel,
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

    FavoriteShowContextViewContent(
        show = show,
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
            show.title,
        ),
    )
}

@Composable
private fun FavoriteShowContextViewContent(
    show: Show,
    state: FavoriteShowContextState,
    modifier: Modifier = Modifier,
    onRemoveClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        PanelMediaCard(
            title = show.title,
            titleOriginal = show.titleOriginal,
            subtitle = remember(show.genres) {
                show.genres.take(2).joinToString(", ") { genre ->
                    genre.replaceFirstChar {
                        it.uppercaseChar()
                    }
                }
            },
            shadow = 4.dp,
            more = false,
            containerColor = Shade910,
            contentImageUrl = show.images?.getPosterUrl(),
            containerImageUrl = show.images?.getFanartUrl(THUMB),
            footerContent = {
                ShowMetaFooter(show)
            },
        )

        if (state.user != null) {
            ShowActionButtons(
                state = state,
                onRemoveClick = onRemoveClick,
            )
        }
    }
}

@Composable
private fun ShowActionButtons(
    state: FavoriteShowContextState,
    onRemoveClick: () -> Unit,
) {
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
            iconSpace = 12.dp,
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
            FavoriteShowContextViewContent(
                state = FavoriteShowContextState(),
                show = PreviewData.show1,
            )
        }
    }
}
