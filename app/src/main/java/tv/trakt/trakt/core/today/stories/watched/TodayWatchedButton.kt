package tv.trakt.trakt.core.today.stories.watched

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import tv.trakt.trakt.core.today.model.TodayForYouItem
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.stories.ui.showTodaySnack
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

private val ButtonSize = 32.dp
private val IconSize = 24.dp
private val LoadingSize = 18.dp

private enum class WatchedConfirm {
    Add,
    Remove,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodayWatchedButton(
    item: TodayForYouItem,
    viewModel: TodayWatchedViewModel,
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
                            TodayWatchedEvent.Added -> R.string.text_info_history_added
                            TodayWatchedEvent.Removed -> R.string.text_info_history_removed
                        },
                    ),
                )
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf<WatchedConfirm?>(null) }

    val confirmVisibleChange by rememberUpdatedState(onConfirmVisibleChange)
    DisposableEffect(confirm) {
        confirmVisibleChange(confirm != null)
        onDispose { confirmVisibleChange(false) }
    }

    val isWholeShow = item is TodayForYouItem.StartWatching && item.media is TodayMedia.ShowMedia

    TodayWatchedButtonContent(
        isWatched = state.isWatched,
        loading = state.loading.isLoading,
        modifier = modifier,
        onClick = {
            when {
                state.isWatched -> confirm = WatchedConfirm.Remove
                isWholeShow -> confirm = WatchedConfirm.Add
                else -> viewModel.addToWatched()
            }
        },
    )

    ConfirmationSheet(
        active = confirm == WatchedConfirm.Add,
        title = stringResource(R.string.confirmation_title_mark_as_watched),
        message = stringResource(R.string.warning_prompt_mark_as_watched_show, item.media.title),
        yesText = stringResource(R.string.button_text_mark_as_watched),
        onYes = {
            confirm = null
            viewModel.addToWatched()
        },
        onNo = { confirm = null },
    )

    RemoveConfirmationSheet(
        active = confirm == WatchedConfirm.Remove,
        title = stringResource(R.string.confirmation_title_remove_from_watched),
        message = stringResource(R.string.warning_prompt_remove_from_watched, item.historyTitle()),
        onYes = {
            confirm = null
            viewModel.removeFromWatched()
        },
        onNo = { confirm = null },
    )
}

@Composable
private fun TodayForYouItem.historyTitle(): String {
    return when (this) {
        is TodayForYouItem.UpNext -> "${show.title} ${episode.seasonEpisodeString()}"
        is TodayForYouItem.StartWatching -> media.title
    }
}

@Composable
private fun TodayWatchedButtonContent(
    isWatched: Boolean,
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
                if (isWatched) R.drawable.ic_check_double else R.drawable.ic_check_2,
            ),
            contentDescription = null,
            tint = when {
                isWatched -> TraktTheme.colors.textPrimary
                else -> TraktTheme.colors.accent
            },
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
            modifier = Modifier.padding(16.dp),
        ) {
            TodayWatchedButtonContent(isWatched = false, onClick = {})
            TodayWatchedButtonContent(isWatched = true, onClick = {})
            TodayWatchedButtonContent(isWatched = false, loading = true, onClick = {})
        }
    }
}
