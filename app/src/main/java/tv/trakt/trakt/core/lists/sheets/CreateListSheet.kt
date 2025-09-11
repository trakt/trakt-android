package tv.trakt.trakt.core.lists.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.core.lists.sheets.create.CreateListView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (sheetActive) {
        TraktBottomSheet(
            sheetState = sheetState,
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
                        localSnack.showSnackbar(localContext.getString(R.string.text_info_list_created))
                    }
                },
                onError = {
                    onDismiss()
                    sheetScope.launch {
                        localSnack.showSnackbar(localContext.getString(R.string.error_text_unexpected_error_short))
                    }
                },
                onListLimitError = {
                    onDismiss()
                    sheetScope.launch {
                        localSnack.showSnackbar(localContext.getString(R.string.error_text_lists_limit))
                    }
                },
            )
        }
    }
}
