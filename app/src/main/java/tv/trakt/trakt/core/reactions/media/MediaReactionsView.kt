@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.reactions.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.LongPress
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.core.reactions.media.ui.MediaReactionsBadge
import tv.trakt.trakt.core.reactions.media.ui.MediaReactionsQuickBarDropdown
import tv.trakt.trakt.core.reactions.media.ui.MediaReactionsSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration

@Composable
internal fun MediaReactionsView(
    viewModel: MediaReactionsViewModel,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snack = LocalSnackbarState.current
    val scope = rememberCoroutineScope()

    val tooltipState = rememberTooltipState(isPersistent = true)
    var pickerSheet by remember { mutableStateOf(false) }

    val isSignedIn = state.user != null
    val isShown = visible && state.loading.isDone && (isSignedIn || state.summary.reactionsCount > 0)

    fun onReactionClick(reaction: MediaReaction) {
        haptic.performHapticFeedback(Confirm)
        viewModel.toggleReaction(reaction)
    }

    AnimatedVisibility(
        visible = isShown,
        enter = fadeIn(tween(200, delayMillis = 350)),
        exit = fadeOut(tween(200, delayMillis = 350)),
        modifier = modifier,
    ) {
        MediaReactionsQuickBarDropdown(
            state = tooltipState,
            userReactions = state.userReactions,
            isLimitReached = state.isLimitReached,
            onReactionClick = ::onReactionClick,
            onMoreClick = {
                tooltipState.dismiss()
                pickerSheet = true
            },
        ) {
            MediaReactionsBadge(
                summary = state.summary,
                userReactions = state.userReactions,
                enabled = isSignedIn,
                modifier = Modifier.onClickCombined(
                    enabled = isSignedIn,
                    indication = false,
                    onClick = {
                        scope.launch {
                            when {
                                tooltipState.isVisible -> tooltipState.dismiss()
                                else -> tooltipState.show()
                            }
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(LongPress)
                        tooltipState.dismiss()
                        pickerSheet = true
                    },
                ),
            )
        }
    }

    MediaReactionsSheet(
        visible = pickerSheet,
        userReactions = state.userReactions,
        isLimitReached = state.isLimitReached,
        onReactionClick = ::onReactionClick,
        onDismiss = { pickerSheet = false },
    )

    LaunchedEffect(state.error) {
        if (state.error == null) return@LaunchedEffect

        val job = launch {
            snack.showSnackbar(context.getString(R.string.error_text_unexpected_error_short))
        }
        delay(ShortSnackDuration)
        job.cancel()
        viewModel.clearError()
    }
}
