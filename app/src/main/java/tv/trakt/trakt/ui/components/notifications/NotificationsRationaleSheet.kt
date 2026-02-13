@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.notifications

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun NotificationsRationaleSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    onOk: () -> Unit = {},
) {
    RationaleSheet(
        state = state,
        active = active,
        onOk = onOk,
    )
}

@Composable
private fun RationaleSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    onOk: () -> Unit = {},
) {
    val sheetScope = rememberCoroutineScope()

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = { onOk() },
        ) {
            NotificationsRationaleView(
                onOk = {
                    onOk()
                    sheetScope.launch {
                        state.hide()
                    }
                },
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, locale = "us")
@Composable
private fun Preview() {
    TraktTheme {
        RationaleSheet(
            state = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
        )
    }
}
