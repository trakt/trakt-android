package tv.trakt.trakt.ui.components.input

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import tv.trakt.trakt.ui.components.TraktBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SingleInputSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    title: String,
    description: String? = null,
    initialInput: String? = null,
    nullable: Boolean = false,
    multiline: Boolean = false,
    onApply: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            SingleInputView(
                title = title,
                description = description,
                initialInput = initialInput,
                nullable = nullable,
                multiline = multiline,
                onApply = { value ->
                    scope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onApply(value)
                                onDismiss()
                            }
                        }
                },
            )
        }
    }
}
