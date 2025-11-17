@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.dateselection

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateSelectionSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    title: String,
    subtitle: String? = null,
    onResult: (result: DateSelectionResult) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            DateSelectionView(
                title = title,
                subtitle = subtitle,
                onNowClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onResult(Now) },
                        onDismiss = onDismiss,
                    )
                },
                onReleaseClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onResult(ReleaseDate) },
                        onDismiss = onDismiss,
                    )
                },
                onOtherClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { },
                        onDismiss = onDismiss,
                    )
                },
                onUnknownClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onResult(UnknownDate) },
                        onDismiss = onDismiss,
                    )
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}

private fun CoroutineScope.dismissWithAction(
    sheet: SheetState,
    action: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    launch {
        sheet.hide()
    }.invokeOnCompletion {
        if (!sheet.isVisible) {
            action()
            onDismiss()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun Preview() {
    TraktTheme {
        DateSelectionSheet(
            state = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            title = "The Matrix",
            onResult = { },
            onDismiss = { },
        )
    }
}
