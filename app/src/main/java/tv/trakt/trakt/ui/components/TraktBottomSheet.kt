package tv.trakt.trakt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TraktBottomSheet(
    sheetState: SheetState,
    containerColor: Color = TraktTheme.colors.dialogContainer,
    contentColor: Color = TraktTheme.colors.dialogContent,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = containerColor,
        contentColor = contentColor,
        scrimColor = Color.Black.copy(alpha = 0.66F),
        onDismissRequest = onDismiss,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 12.dp)
                    .background(
                        color = TraktTheme.colors.dialogContent,
                        shape = RoundedCornerShape(100),
                    )
                    .size(36.dp, 4.dp),
            )
        },
    ) {
        // Set light system bars appearance fix for dialogs
        val view = LocalView.current
        (view.parent as? DialogWindowProvider)?.window?.let { window ->
            SideEffect {
                with(WindowCompat.getInsetsController(window, view)) {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }

        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TraktBottomSheetPreview() {
    TraktTheme {
        TraktBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
        ) {
            Text(text = "Sample bottom sheet")
        }
    }
}
