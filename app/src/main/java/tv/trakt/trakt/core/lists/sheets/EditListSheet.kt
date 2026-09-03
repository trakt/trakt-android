package tv.trakt.trakt.core.lists.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalResources
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.sheets.edit.EditListView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import kotlin.random.Random.Default.nextInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditListSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean,
    list: CustomList?,
    onListEdited: (() -> Unit)? = null,
    onListDeleted: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localRes = LocalResources.current

    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(list) { nextInt().toString() }

    if (active && list != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            EditListView(
                initialList = list,
                viewModel = koinViewModel(
                    key = viewModelKey,
                ),
                onListEdited = {
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onListEdited?.invoke()
                                onDismiss()
                            }
                        }

                    sheetScope.launch {
                        val job = sheetScope.launch {
                            localSnack.showSnackbar(localRes.getString(R.string.text_info_list_updated))
                        }
                        delay(ShortSnackDuration)
                        job.cancel()
                    }
                },
                onListDeleted = {
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onListDeleted?.invoke()
                                onDismiss()
                            }
                        }

                    sheetScope.launch {
                        val job = sheetScope.launch {
                            localSnack.showSnackbar(localRes.getString(R.string.text_info_list_deleted))
                        }
                        delay(ShortSnackDuration)
                        job.cancel()
                    }
                },
                onError = {
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }

                    sheetScope.launch {
                        val job = sheetScope.launch {
                            localSnack.showSnackbar(localRes.getString(R.string.error_text_unexpected_error_short))
                        }
                        delay(ShortSnackDuration)
                        job.cancel()
                    }
                },
            )
        }
    }
}
