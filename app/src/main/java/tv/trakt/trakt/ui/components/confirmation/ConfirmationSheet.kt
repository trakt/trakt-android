package tv.trakt.trakt.ui.components.confirmation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfirmationSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    title: String,
    message: String,
    onYes: () -> Unit = {},
    onNo: () -> Unit = {},
) {
    val sheetScope = rememberCoroutineScope()

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = { onNo() },
        ) {
            ConfirmationView(
                title = title,
                message = message,
                onYes = {
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onYes()
                            }
                        }
                },
                onNo = {
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onNo()
                            }
                        }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun Preview() {
    TraktTheme {
        ConfirmationSheet(
            state = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            title = "Are you sure?",
            message = "This action cannot be undone.",
        )
    }
}
