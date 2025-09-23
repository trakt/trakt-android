@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.shows.ui.context

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.common.ui.theme.colors.White
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowContextView(
    show: Show,
    viewModel: ShowContextViewModel,
    modifier: Modifier = Modifier,
    onAddWatched: (Show) -> Unit,
    onAddWatchlist: (Show) -> Unit,
    onRemoveWatched: (Show) -> Unit,
    onRemoveWatchlist: (Show) -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingWatched, state.loadingWatchlist) {
        when {
            state.loadingWatched == LoadingState.DONE -> when {
                !state.isWatched -> onAddWatched(show)
                else -> onRemoveWatched(show)
            }
            state.loadingWatchlist == LoadingState.DONE -> when {
                !state.isWatchlist -> onAddWatchlist(show)
                else -> onRemoveWatchlist(show)
            }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    ShowContextViewContent(
        show = show,
        state = state,
        modifier = modifier,
        onWatchedClick = {
            when {
                state.isWatched -> confirmRemoveWatchedSheet = true
                else -> viewModel.addToWatched()
            }
        },
        onWatchlistClick = {
            when {
                state.isWatchlist -> confirmRemoveWatchlistSheet = true
                else -> viewModel.addToWatchlist()
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
}

@Composable
private fun ShowContextViewContent(
    show: Show,
    state: ShowContextState,
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
            containerColor = Shade910,
            contentImageUrl = show.images?.getPosterUrl(),
            containerImageUrl = show.images?.getFanartUrl(THUMB),
            footerContent = {
                Row(
                    horizontalArrangement = Arrangement.Absolute.spacedBy(TraktTheme.spacing.chipsSpace),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val epsString = stringResource(
                        R.string.tag_text_number_of_episodes,
                        show.airedEpisodes,
                    )

                    val metaString = remember {
                        val separator = "  •  "
                        buildString {
                            show.released?.let {
                                append(it.year)
                            }
                            if (show.airedEpisodes > 0) {
                                if (isNotEmpty()) append(separator)
                                append(epsString)
                            }
                            if (!show.certification.isNullOrBlank()) {
                                if (isNotEmpty()) append(separator)
                                append(show.certification)
                            }
                        }
                    }

                    Text(
                        text = metaString,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                    ) {
                        val grayFilter = remember {
                            ColorFilter.colorMatrix(
                                ColorMatrix().apply {
                                    setToSaturation(0F)
                                },
                            )
                        }
                        val whiteFilter = remember {
                            ColorFilter.tint(White)
                        }

                        Spacer(modifier = Modifier.weight(1F))

                        Image(
                            painter = painterResource(R.drawable.ic_trakt_icon),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            colorFilter = if (show.rating.rating > 0) whiteFilter else grayFilter,
                        )
                        Text(
                            text = if (show.rating.rating > 0) "${show.rating.ratingPercent}%" else "-",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                        )
                    }
                }
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
    state: ShowContextState,
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
        modifier = Modifier.padding(top = 24.dp),
    ) {
        if (isReleased) {
            if (state.isWatched) {
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
            ShowContextViewContent(
                state = ShowContextState(),
                show = PreviewData.show1,
            )
        }
    }
}
