@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.shows.ui.context

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.customAnnotatedString
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowContextView(
    show: Show,
    viewModel: ShowContextViewModel,
    modifier: Modifier = Modifier,
    showWatched: Boolean,
    showRecommended: Boolean,
    onAddWatched: (Show) -> Unit,
    onAddWatchlist: (Show) -> Unit,
    onRemoveWatched: (Show) -> Unit,
    onRemoveWatchlist: (Show) -> Unit,
    onHideRecommendation: (Show) -> Unit,
    onWhyThis: () -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmAddWatchedSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }
    var confirmHideRecommendationSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingWatched, state.loadingWatchlist) {
        when {
            state.loadingWatched == LoadingState.Done -> when {
                !state.isWatched -> onAddWatched(show)
                else -> onRemoveWatched(show)
            }

            state.loadingWatchlist == LoadingState.Done -> when {
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
        showWatched = showWatched,
        showRecommended = showRecommended,
        onWatchedClick = {
            when {
                state.isWatched -> confirmRemoveWatchedSheet = true
                else -> confirmAddWatchedSheet = true
            }
        },
        onWatchlistClick = {
            when {
                state.isWatchlist -> confirmRemoveWatchlistSheet = true
                else -> viewModel.addToWatchlist()
            }
        },
        onHideRecommendationClick = {
            confirmHideRecommendationSheet = true
        },
        onWhyThisClick = onWhyThis,
        modifier = modifier,
    )

    ConfirmationSheet(
        active = confirmAddWatchedSheet,
        onYes = {
            confirmAddWatchedSheet = false
            dateSheet = true
        },
        onNo = { confirmAddWatchedSheet = false },
        title = stringResource(R.string.button_text_mark_as_watched),
        annotatedMessage = customAnnotatedString(
            stringResource(
                R.string.warning_prompt_mark_as_watched_show_android,
                show.airedEpisodes,
                show.title,
            ),
            style = TraktTheme.typography.paragraph.toSpanStyle()
                .copy(
                    fontWeight = FontWeight.W600,
                    textDecoration = TextDecoration.Underline,
                ),
        ),
        holdToYes = true,
        yesText = stringResource(R.string.button_text_hold_to_confirm),
    )

    RemoveConfirmationSheet(
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

    RemoveConfirmationSheet(
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

    RemoveConfirmationSheet(
        active = confirmHideRecommendationSheet,
        onYes = {
            confirmHideRecommendationSheet = false
            onHideRecommendation(show)
        },
        onNo = { confirmHideRecommendationSheet = false },
        title = stringResource(R.string.button_text_hide_recommendation),
        message = stringResource(
            R.string.warning_prompt_hide_recommendation,
            show.title,
        ),
    )

    DateSelectionSheet(
        active = dateSheet,
        title = show.title,
        onResult = viewModel::addToWatched,
        onDismiss = {
            dateSheet = false
        },
    )
}

@Composable
private fun ShowContextViewContent(
    show: Show,
    state: ShowContextState,
    showWatched: Boolean,
    showRecommended: Boolean,
    modifier: Modifier = Modifier,
    onWatchedClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = {},
    onHideRecommendationClick: () -> Unit = {},
    onWhyThisClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = show.title,
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

            ShowMetaFooter(
                show = show,
                secondary = true,
                textStyle = TraktTheme.typography.paragraphSmaller,
            )
        }

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(TraktTheme.colors.separator)
                .fillMaxWidth()
                .height(1.dp),
        )

        if (state.user != null) {
            ShowActionButtons(
                show = show,
                state = state,
                showWatched = showWatched,
                showRecommended = showRecommended,
                onWatchedClick = onWatchedClick,
                onWatchlistClick = onWatchlistClick,
                onHideRecommendationClick = onHideRecommendationClick,
                onWhyThisClick = onWhyThisClick,
                modifier = Modifier
                    .padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun ShowActionButtons(
    modifier: Modifier = Modifier,
    show: Show,
    state: ShowContextState,
    showWatched: Boolean,
    showRecommended: Boolean,
    onWatchedClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onHideRecommendationClick: () -> Unit,
    onWhyThisClick: () -> Unit,
) {
    val isReleased = show.rememberReleased()

    val isLoadingOrDone =
        state.loadingWatched.isLoading ||
            state.loadingWatchlist.isLoading ||
            state.loadingWatchlist.isDone ||
            state.loadingWatched.isDone

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier,
    ) {
        if (showRecommended) {
            GhostButton(
                enabled = !isLoadingOrDone,
                text = stringResource(R.string.button_text_view_recommendation_sources),
                onClick = onWhyThisClick,
                iconSize = 22.dp,
                iconSpace = 16.dp,
                icon = painterResource(R.drawable.ic_discover_on),
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -6.dp.toPx()
                    },
            )
        }

        if (isReleased && showWatched) {
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
                            translationX = -6.dp.toPx()
                        },
                )
            } else {
                GhostButton(
                    enabled = !isLoadingOrDone,
                    loading = state.loadingWatched.isLoading || state.loadingWatched.isDone,
                    text = stringResource(R.string.button_text_track),
                    iconSize = 22.dp,
                    iconSpace = 16.dp,
                    onClick = onWatchedClick,
                    icon = painterResource(R.drawable.ic_check_2),
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -6.dp.toPx()
                        },
                )
            }
        }

        GhostButton(
            enabled = !isLoadingOrDone,
            loading = state.loadingWatchlist.isLoading || state.loadingWatchlist.isDone,
            text = when {
                state.isWatchlist -> stringResource(R.string.button_text_remove_from_watchlist)
                else -> stringResource(R.string.button_text_watchlist)
            },
            onClick = onWatchlistClick,
            iconSize = 25.dp,
            iconSpace = 16.dp,
            icon = when {
                state.isWatchlist -> painterResource(R.drawable.ic_bookmark_on)
                else -> painterResource(R.drawable.ic_bookmark_off)
            },
            modifier = Modifier
                .graphicsLayer {
                    translationX = -8.dp.toPx()
                },
        )

        if (showRecommended) {
            GhostButton(
                enabled = !isLoadingOrDone,
                loading = state.loadingWatchlist.isLoading || state.loadingWatchlist.isDone,
                text = stringResource(R.string.button_text_hide_recommendation),
                onClick = onHideRecommendationClick,
                iconSize = 22.dp,
                iconSpace = 16.dp,
                icon = painterResource(R.drawable.ic_eye_off),
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -6.dp.toPx()
                    },
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
    locale = "en",
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ShowContextViewContent(
                state = ShowContextState(
                    user = PreviewData.user1,
                ),
                show = PreviewData.show1,
                showWatched = true,
                showRecommended = true,
            )
        }
    }
}
