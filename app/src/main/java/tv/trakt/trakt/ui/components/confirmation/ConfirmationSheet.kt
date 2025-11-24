@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.confirmation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun RemoveConfirmationSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    title: String,
    message: String,
    onYes: () -> Unit = {},
    onNo: () -> Unit = {},
) {
    ConfirmationSheet(
        state = state,
        active = active,
        title = title,
        message = message,
        yesColor = Red500,
        onYes = onYes,
        onNo = onNo,
    )
}

@Composable
internal fun ConfirmationSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    title: String,
    message: String,
    yesColor: Color = TraktTheme.colors.primaryButtonContainer,
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
                yesColor = yesColor,
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
