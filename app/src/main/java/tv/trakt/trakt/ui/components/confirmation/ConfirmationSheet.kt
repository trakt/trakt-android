package tv.trakt.trakt.ui.components.confirmation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = { onNo() },
        ) {
            ConfirmationView(
                title = title,
                message = message,
                onYes = onYes,
                onNo = onNo,
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
