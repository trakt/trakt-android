package tv.trakt.trakt.core.settings.features.notifications

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment
import tv.trakt.trakt.ui.components.TraktBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdjustNotificationTimeSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean,
    initial: DeliveryAdjustment,
    onApply: (DeliveryAdjustment) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            AdjustNotificationTimeView(
                current = initial,
                onApply = { time ->
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onApply(time)
                                onDismiss()
                            }
                        }
                },
            )
        }
    }
}
