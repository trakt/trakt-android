@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.calendar.ui.context.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.core.calendar.ui.context.CalendarEpisodeContextView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import kotlin.random.Random.Default.nextInt

@Composable
internal fun CalendarEpisodeContextSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    item: CalendarItem.EpisodeItem?,
    showWatchlist: Boolean = true,
    showDrop: Boolean = false,
    onDataChange: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(item) { nextInt().toString() }

    if (item != null) {
        val localSnack = LocalSnackbarState.current
        val localRes = LocalResources.current

        fun dismissWithMessage(message: String) {
            sheetScope.dismissWithMessage(
                state = state,
                snackHost = localSnack,
                onDismiss = onDismiss,
                message = message,
            )
        }

        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            CalendarEpisodeContextView(
                item = item,
                showWatchlist = showWatchlist,
                showDrop = showDrop,
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(item) },
                ),
                onAddWatched = {
                    dismissWithMessage(localRes.getString(R.string.text_info_history_added))
                },
                onRemoveWatched = {
                    dismissWithMessage(localRes.getString(R.string.text_info_history_removed))
                },
                onAddWatchlist = {
                    dismissWithMessage(localRes.getString(R.string.text_info_watchlist_added))
                },
                onRemoveWatchlist = {
                    dismissWithMessage(localRes.getString(R.string.text_info_watchlist_removed))
                },
                onDropped = {
                    onDataChange()
                    dismissWithMessage(localRes.getString(R.string.text_info_show_dropped))
                },
                onError = {
                    dismissWithMessage(localRes.getString(R.string.error_text_unexpected_error_short))
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}

private fun CoroutineScope.dismissWithMessage(
    state: SheetState,
    onDismiss: () -> Unit,
    snackHost: SnackbarHostState,
    message: String,
) {
    launch { state.hide() }
        .invokeOnCompletion {
            if (!state.isVisible) {
                onDismiss()
            }
        }

    launch {
        val job = this@dismissWithMessage.launch {
            snackHost.showSnackbar(message)
        }
        delay(ShortSnackDuration)
        job.cancel()
    }
}
