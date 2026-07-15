@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.welcome.sheet

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun WelcomeSheet(
    visible: Boolean,
    name: String? = null,
    isVip: Boolean = false,
    state: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    ),
    onDismiss: () -> Unit = {},
    onVipClick: () -> Unit = {},
    onGetStartedClick: () -> Unit = {},
) {
    val sheetScope = rememberCoroutineScope()

    if (visible) {
        TraktBottomSheet(
            handle = false,
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            WelcomeView(
                modifier = Modifier.fillMaxHeight(0.9F),
                name = name,
                isVip = isVip,
                onVipClick = {
                    sheetScope.dismissWithAction(
                        sheet = state,
                        onDismiss = onDismiss,
                        action = onVipClick,
                    )
                },
                onStartExploringClick = {
                    sheetScope.dismissWithAction(
                        sheet = state,
                        onDismiss = onDismiss,
                        action = onGetStartedClick,
                    )
                },
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
