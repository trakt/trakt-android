package tv.trakt.trakt.core.comments.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Sheet state that blocks hiding while [hasChanges] is true and calls [onDiscardRequest] instead.
 * This also applies to [SheetState.hide], so clear the changes before hiding programmatically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberDiscardGuardedSheetState(
    hasChanges: () -> Boolean,
    onDiscardRequest: () -> Unit,
): SheetState {
    val currentHasChanges by rememberUpdatedState(hasChanges)
    val currentOnDiscardRequest by rememberUpdatedState(onDiscardRequest)

    // Kept as one instance because the sheet state is remembered with it as a key.
    val confirmValueChange: (SheetValue) -> Boolean = remember {
        { value ->
            val isBlocked = value == Hidden && currentHasChanges()
            if (isBlocked) {
                currentOnDiscardRequest()
            }
            !isBlocked
        }
    }

    return rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
        confirmValueChange = confirmValueChange,
    )
}
