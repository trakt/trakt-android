@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.personal.features.context.show

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
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
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
internal fun ListShowContextView(
    show: Show,
    list: CustomList,
    viewModel: ListShowContextViewModel,
    modifier: Modifier = Modifier,
    onAddWatched: (Show) -> Unit,
    onAddWatchlist: (Show) -> Unit,
    onRemoveWatched: (Show) -> Unit,
    onRemoveWatchlist: (Show) -> Unit,
    onRemoveList: (Show) -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmAddWatchedSheet by remember { mutableStateOf(false) }
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
                !state.isWatched || state.isWatchlist -> onAddWatched(show)
                else -> onRemoveWatched(show)
            }
            state.loadingWatchlist == LoadingState.DONE -> when {
                !state.isWatchlist -> onAddWatchlist(show)
                else -> onRemoveWatchlist(show)
            }
            state.loadingList == LoadingState.DONE -> {
                onRemoveList(show)
            }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    ListShowContextViewContent(
        show = show,
        state = state,
        modifier = modifier,
        onWatchedClick = {
            when {
                state.isWatched && !state.isWatchlist -> confirmRemoveWatchedSheet = true
                else -> confirmAddWatchedSheet = true
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
        active = confirmAddWatchedSheet,
        onYes = {
            confirmAddWatchedSheet = false
            viewModel.addToWatched()
        },
        onNo = { confirmAddWatchedSheet = false },
        title = stringResource(R.string.button_text_mark_as_watched),
        message = stringResource(
            R.string.warning_prompt_mark_as_watched_show,
            show.title,
        ),
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
        active = confirmRemoveWatchedSheet,
        onYes = {
            confirmRemoveWatchedSheet = false
            viewModel.removeFromWatched()
        },
        onNo = { confirmRemoveWatchedSheet = false },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            show.title,
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
            show.title,
            list.name,
        ),
    )
}

@Composable
private fun ListShowContextViewContent(
    show: Show,
    state: ListShowContextState,
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

        ActionButtons(
            show = show,
            state = state,
            onWatchedClick = onWatchedClick,
            onWatchlistClick = onWatchlistClick,
            onRemoveListClick = onRemoveListClick,
        )
    }
}

@Composable
private fun ActionButtons(
    show: Show,
    state: ListShowContextState,
    onWatchedClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onRemoveListClick: () -> Unit,
) {
    val isReleased = remember {
        show.released?.isNowOrBefore() ?: false
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
        modifier = Modifier.padding(top = 20.dp),
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
                            translationX = -6.dp.toPx()
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
                            translationX = -6.dp.toPx()
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
                    translationX = -6.dp.toPx()
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
            ListShowContextViewContent(
                state = ListShowContextState(),
                show = PreviewData.show1,
            )
        }
    }
}
