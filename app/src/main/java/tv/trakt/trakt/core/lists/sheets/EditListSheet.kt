package tv.trakt.trakt.core.lists.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.sheets.edit.EditListView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditListSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean,
    list: CustomList?,
    onListEdited: () -> Unit,
    onListDeleted: () -> Unit,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localContext = LocalContext.current

    val sheetScope = rememberCoroutineScope()

    if (active && list != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            EditListView(
                initialList = list,
                viewModel = koinViewModel(
                    key = Random.Default.nextInt().toString(),
                ),
                onListEdited = {
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onListEdited()
                                onDismiss()
                            }
                        }

                    sheetScope.launch {
                        val job = sheetScope.launch {
                            localSnack.showSnackbar(localContext.getString(R.string.text_info_list_updated))
                        }
                        delay(SNACK_DURATION_SHORT)
                        job.cancel()
                    }
                },
                onListDeleted = {
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onListDeleted()
                                onDismiss()
                            }
                        }

                    sheetScope.launch {
                        val job = sheetScope.launch {
                            localSnack.showSnackbar(localContext.getString(R.string.text_info_list_deleted))
                        }
                        delay(SNACK_DURATION_SHORT)
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
                            localSnack.showSnackbar(localContext.getString(R.string.error_text_unexpected_error_short))
                        }
                        delay(SNACK_DURATION_SHORT)
                        job.cancel()
                    }
                },
            )
        }
    }
}
