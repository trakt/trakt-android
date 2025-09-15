package tv.trakt.trakt.core.lists.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.core.lists.sheets.create.CreateListView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateListSheet(
    sheetActive: Boolean,
    onListCreated: () -> Unit,
    onDismiss: () -> Unit,
) {
    val localSnack = LocalSnackbarState.current
    val localContext = LocalContext.current

    val sheetScope = rememberCoroutineScope()

    if (sheetActive) {
        TraktBottomSheet(
            onDismiss = onDismiss,
        ) {
            CreateListView(
                viewModel = koinViewModel(
                    key = Random.Default.nextInt().toString(),
                ),
                onListCreated = {
                    onListCreated()
                    onDismiss()
                    sheetScope.launch {
                        val job = sheetScope.launch {
                            localSnack.showSnackbar(localContext.getString(R.string.text_info_list_created))
                        }
                        delay(SNACK_DURATION_SHORT)
                        job.cancel()
                    }
                },
                onError = {
                    onDismiss()
                    sheetScope.launch {
                        val job = sheetScope.launch {
                            localSnack.showSnackbar(localContext.getString(R.string.error_text_unexpected_error_short))
                        }
                        delay(SNACK_DURATION_SHORT)
                        job.cancel()
                    }
                },
                onListLimitError = {
                    onDismiss()
                    sheetScope.launch {
                        val job = sheetScope.launch {
                            localSnack.showSnackbar(localContext.getString(R.string.error_text_lists_limit))
                        }
                        delay(SNACK_DURATION_SHORT)
                        job.cancel()
                    }
                },
            )
        }
    }
}
