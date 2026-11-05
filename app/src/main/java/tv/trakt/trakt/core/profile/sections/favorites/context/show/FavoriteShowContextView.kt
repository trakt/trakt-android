@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.favorites.context.show

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.customAnnotatedString
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun FavoriteShowContextView(
    show: Show,
    viewModel: FavoriteShowContextViewModel,
    modifier: Modifier = Modifier,
    onRemovedFromFavorites: () -> Unit,
    onAddWatched: () -> Unit,
    onRemoveWatched: () -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveSheet by remember { mutableStateOf(false) }
    var confirmAddWatchedSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingFavorite, state.loadingWatched) {
        when {
            state.loadingFavorite == LoadingState.Done -> onRemovedFromFavorites()
            state.loadingWatched == LoadingState.Done -> when {
                state.isWatched -> onRemoveWatched()
                else -> onAddWatched()
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
        onWatchedClick = {
            when {
                state.isWatched -> confirmRemoveWatchedSheet = true
                else -> confirmAddWatchedSheet = true
            }
        },
    )

    RemoveConfirmationSheet(
        active = confirmRemoveSheet,
        onYes = {
            confirmRemoveSheet = false
            viewModel.removeFromFavorites()
        },
        onNo = { confirmRemoveSheet = false },
        title = stringResource(R.string.button_text_remove_from_favorites),
        message = stringResource(
            R.string.warning_prompt_remove_from_favorites,
            show.title,
        ),
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
private fun FavoriteShowContextViewContent(
    show: Show,
    state: FavoriteShowContextState,
    modifier: Modifier = Modifier,
    onRemoveClick: () -> Unit = {},
    onWatchedClick: () -> Unit = {},
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
                onRemoveClick = onRemoveClick,
                onWatchedClick = onWatchedClick,
            )
        }
    }
}

@Composable
private fun ShowActionButtons(
    show: Show,
    state: FavoriteShowContextState,
    onRemoveClick: () -> Unit,
    onWatchedClick: () -> Unit,
) {
    val isReleased = show.rememberReleased()
    val isLoadingOrDone =
        state.loadingFavorite.isLoading ||
            state.loadingFavorite.isDone ||
            state.loadingWatched.isLoading ||
            state.loadingWatched.isDone

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = Modifier.padding(top = 14.dp),
    ) {
        if (isReleased) {
            GhostButton(
                enabled = !isLoadingOrDone,
                loading = state.loadingWatched.isLoading || state.loadingWatched.isDone,
                text = when {
                    state.isWatched -> stringResource(R.string.button_text_remove_from_history)
                    else -> stringResource(R.string.button_text_track)
                },
                onClick = onWatchedClick,
                icon = when {
                    state.isWatched -> painterResource(R.drawable.ic_trash)
                    else -> painterResource(R.drawable.ic_check_2)
                },
                iconSize = 22.dp,
                iconSpace = 11.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -7.dp.toPx()
                    },
            )
        }

        GhostButton(
            enabled = !isLoadingOrDone,
            loading = state.loadingFavorite.isLoading || state.loadingFavorite.isDone,
            text = stringResource(R.string.button_text_remove_from_favorites),
            onClick = onRemoveClick,
            icon = painterResource(R.drawable.ic_close),
            iconSize = 22.dp,
            iconSpace = 11.dp,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -7.dp.toPx()
                },
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            FavoriteShowContextViewContent(
                state = FavoriteShowContextState(
                    user = PreviewData.user1,
                ),
                show = PreviewData.show1,
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@PreviewTraktLightDark
@Composable
private fun PreviewWatched() {
    TraktThemePreview {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            FavoriteShowContextViewContent(
                state = FavoriteShowContextState(
                    user = PreviewData.user1,
                    isWatched = true,
                ),
                show = PreviewData.show1,
            )
        }
    }
}
