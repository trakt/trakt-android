@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.personal.features.context.show.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.features.manage.ManageListsSheet
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.ListShowContextView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import tv.trakt.trakt.ui.snackbar.showWatchlistAdded
import kotlin.random.Random.Default.nextInt

@Composable
internal fun ListShowContextSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    show: Show?,
    list: CustomList?,
    onRemoveListItem: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(show, list) { nextInt().toString() }
    var listsTarget by remember { mutableStateOf<ManageListsTarget?>(null) }

    if (show != null && list != null) {
        val localSnack = LocalSnackbarState.current
        val localRes = LocalResources.current

        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ListShowContextView(
                show = show,
                list = list,
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(show, list) },
                ),
                onAddWatched = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localRes.getString(R.string.text_info_history_added),
                    )
                },
                onAddWatchlist = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = null,
                    )
                    sheetScope.launch {
                        localSnack.showWatchlistAdded(
                            resources = localRes,
                            title = show.title,
                            onChangeList = { listsTarget = ManageListsTarget.ShowTarget(show) },
                        )
                    }
                },
                onRemoveWatched = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localRes.getString(R.string.text_info_history_removed),
                    )
                },
                onRemoveWatchlist = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localRes.getString(R.string.text_info_watchlist_removed),
                    )
                },
                onRemoveList = {
                    onRemoveListItem?.invoke()
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localRes.getString(R.string.text_info_list_removed),
                    )
                },
                onError = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localRes.getString(R.string.error_text_unexpected_error_short),
                    )
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }

    ManageListsSheet(
        target = listsTarget,
        onDismiss = { listsTarget = null },
    )
}

private fun CoroutineScope.dismissWithMessage(
    state: SheetState,
    onDismiss: () -> Unit,
    snackHost: SnackbarHostState,
    message: String?,
) {
    launch { state.hide() }
        .invokeOnCompletion {
            if (!state.isVisible) {
                onDismiss()
            }
        }

    if (message == null) return

    launch {
        val job = this@dismissWithMessage.launch {
            snackHost.showSnackbar(message)
        }
        delay(ShortSnackDuration)
        job.cancel()
    }
}
