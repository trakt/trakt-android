@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows

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
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun WatchlistShowContextView(
    show: Show,
    viewModel: WatchlistShowContextViewModel,
    modifier: Modifier = Modifier,
    addLocally: Boolean,
    onAddWatched: (Show) -> Unit,
    onRemoveWatchlist: (Show) -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var confirmAddWatchedSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf<Show?>(null) }

    LaunchedEffect(state.loadingWatched, state.loadingWatchlist) {
        when {
            state.loadingWatched == DONE -> onAddWatched(show)
            state.loadingWatchlist == DONE -> onRemoveWatchlist(show)
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    WatchlistShowContextViewContent(
        show = show,
        state = state,
        modifier = modifier,
        onWatchedClick = {
            if (addLocally) {
                confirmAddWatchedSheet = true
            } else {
                onAddWatched(show)
            }
        },
        onWatchlistClick = {
            confirmRemoveWatchlistSheet = true
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
            show.title,
        ),
    )

    ConfirmationSheet(
        active = confirmAddWatchedSheet,
        onYes = {
            confirmAddWatchedSheet = false
            dateSheet = show
        },
        onNo = { confirmAddWatchedSheet = false },
        title = stringResource(R.string.button_text_mark_as_watched),
        message = stringResource(
            R.string.warning_prompt_mark_as_watched_show,
            show.title,
        ),
    )

    DateSelectionSheet(
        show = dateSheet,
        onDateSelected = viewModel::addToWatched,
        onDismiss = {
            dateSheet = null
        },
    )
}

@Composable
private fun WatchlistShowContextViewContent(
    show: Show,
    state: WatchlistShowContextState,
    modifier: Modifier = Modifier,
    onWatchedClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = {},
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
                ShowMetaFooter(show = show)
            },
        )

        if (state.user != null) {
            ShowActionButtons(
                show = show,
                state = state,
                onWatchedClick = onWatchedClick,
                onWatchlistClick = onWatchlistClick,
            )
        }
    }
}

@Composable
private fun ShowActionButtons(
    show: Show,
    state: WatchlistShowContextState,
    onWatchedClick: () -> Unit,
    onWatchlistClick: () -> Unit,
) {
    val isReleased = remember {
        show.released?.isNowOrBefore() ?: false
    }

    val isLoadingOrDone =
        state.loadingWatched.isLoading ||
            state.loadingWatchlist.isLoading ||
            state.loadingWatchlist.isDone ||
            state.loadingWatched.isDone

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = Modifier.padding(top = 20.dp),
    ) {
        if (isReleased) {
            GhostButton(
                enabled = !isLoadingOrDone,
                loading = state.loadingWatched.isLoading || state.loadingWatched.isDone,
                text = stringResource(R.string.button_text_mark_as_watched),
                iconSize = 22.dp,
                iconSpace = 16.dp,
                onClick = onWatchedClick,
                icon = painterResource(R.drawable.ic_check),
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -6.dp.toPx()
                    },
            )
        }

        GhostButton(
            enabled = !isLoadingOrDone,
            loading = state.loadingWatchlist.isLoading || state.loadingWatchlist.isDone,
            text = stringResource(R.string.button_text_watchlist),
            onClick = onWatchlistClick,
            iconSize = 22.dp,
            iconSpace = 17.dp,
            icon = painterResource(R.drawable.ic_minus),
            modifier = Modifier
                .graphicsLayer {
                    translationX = -6.dp.toPx()
                },
        )
    }
}

@Composable
private fun DateSelectionSheet(
    show: Show?,
    onDateSelected: (DateSelectionResult?) -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = show != null,
        title = show?.title.orEmpty(),
        subtitle = null,
        onResult = {
            if (show == null) return@DateSelectionSheet
            onDateSelected(it)
        },
        onDismiss = onDismiss,
    )
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
            WatchlistShowContextViewContent(
                state = WatchlistShowContextState(),
                show = PreviewData.show1,
            )
        }
    }
}
