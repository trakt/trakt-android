@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.sections.personal.features.context.item

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
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.features.manage.ManageListsSheet
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.CustomListItem.EpisodeItem
import tv.trakt.trakt.core.lists.model.CustomListItem.MovieItem
import tv.trakt.trakt.core.lists.model.CustomListItem.SeasonItem
import tv.trakt.trakt.core.lists.model.CustomListItem.ShowItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import kotlin.random.Random.Default.nextInt

@Composable
internal fun ListItemContextSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    item: CustomListItem?,
    list: CustomList?,
    removable: Boolean,
    onRemoveListItem: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(item, list) { nextInt().toString() }
    var listsTarget by remember { mutableStateOf<ManageListsTarget?>(null) }

    if (item != null && list != null) {
        val localSnack = LocalSnackbarState.current
        val localRes = LocalResources.current

        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ListItemContextView(
                item = item,
                list = list,
                removable = removable,
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(item, list) },
                ),
                onAddWatched = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localRes.getString(R.string.text_info_history_added),
                    )
                },
                onRemoveWatched = {
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localRes.getString(R.string.text_info_history_removed),
                    )
                },
                onRemoveList = {
                    onRemoveListItem()
                    sheetScope.dismissWithMessage(
                        state = state,
                        snackHost = localSnack,
                        onDismiss = onDismiss,
                        message = localRes.getString(R.string.text_info_list_removed),
                    )
                },
                onListsClick = {
                    listsTarget = item.toListsTarget()
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
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

private fun CustomListItem.toListsTarget(): ManageListsTarget {
    return when (this) {
        is MovieItem -> ManageListsTarget.MovieTarget(movie)
        is ShowItem -> ManageListsTarget.ShowTarget(show)
        is SeasonItem -> ManageListsTarget.SeasonTarget(show, season)
        is EpisodeItem -> ManageListsTarget.EpisodeTarget(show, episode)
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
