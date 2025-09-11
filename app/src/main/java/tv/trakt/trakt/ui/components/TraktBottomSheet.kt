package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TraktBottomSheet(
    sheetState: SheetState,
    containerColor: Color = TraktTheme.colors.dialogContainer,
    contentColor: Color = TraktTheme.colors.textPrimary,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = containerColor,
        contentColor = contentColor,
        scrimColor = Color.Black.copy(alpha = 0.55F),
        onDismissRequest = onDismiss,
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
