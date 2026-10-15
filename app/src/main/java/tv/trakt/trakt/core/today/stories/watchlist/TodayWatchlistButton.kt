package tv.trakt.trakt.core.today.stories.watchlist

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.stories.ui.showTodaySnack
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

private val ButtonSize = 32.dp
private val IconSize = 24.dp
private val LoadingSize = 18.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodayWatchlistButton(
    media: TodayMedia,
    viewModel: TodayWatchlistViewModel,
    modifier: Modifier = Modifier,
    onConfirmVisibleChange: (Boolean) -> Unit,
) {
    val snack = LocalSnackbarState.current
    val haptic = LocalHapticFeedback.current
    val resources = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            haptic.performHapticFeedback(Confirm)
            this@LaunchedEffect.launch {
                snack.showTodaySnack(
                    resources.getString(
                        when (event) {
                            TodayWatchlistEvent.Added -> R.string.text_info_watchlist_added
                            TodayWatchlistEvent.Removed -> R.string.text_info_watchlist_removed
                        },
                    ),
                )
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmRemove by remember { mutableStateOf(false) }

    val confirmVisibleChange by rememberUpdatedState(onConfirmVisibleChange)
    DisposableEffect(confirmRemove) {
        confirmVisibleChange(confirmRemove)
        onDispose { confirmVisibleChange(false) }
    }

    val isWatchlisted = state.isWatchlisted ?: return

    TodayWatchlistButtonContent(
        isWatchlisted = isWatchlisted,
        loading = state.loading.isLoading,
        modifier = modifier,
        onClick = {
            if (isWatchlisted) {
                confirmRemove = true
            } else {
                viewModel.addToWatchlist()
            }
        },
    )

    RemoveConfirmationSheet(
        active = confirmRemove,
        onYes = {
            confirmRemove = false
            viewModel.removeFromWatchlist()
        },
        onNo = { confirmRemove = false },
        title = stringResource(R.string.button_text_watchlist),
        message = stringResource(
            R.string.warning_prompt_remove_from_watchlist,
            media.title,
        ),
    )
}

@Composable
private fun TodayWatchlistButtonContent(
    isWatchlisted: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(ButtonSize)
            .onClick(
                enabled = !loading,
                onClick = onClick,
            ),
    ) {
        if (loading) {
            FilmProgressIndicator(
                size = LoadingSize,
                color = TraktTheme.colors.textPrimary,
            )
            return@Box
        }

        Icon(
            painter = painterResource(
                if (isWatchlisted) R.drawable.ic_bookmark_on else R.drawable.ic_bookmark_off,
            ),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(IconSize),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Row(
            horizontalArrangement = spacedBy(16.dp),
        ) {
            TodayWatchlistButtonContent(isWatchlisted = false, onClick = {})
            TodayWatchlistButtonContent(isWatchlisted = true, onClick = {})
            TodayWatchlistButtonContent(isWatchlisted = false, loading = true, onClick = {})
        }
    }
}
