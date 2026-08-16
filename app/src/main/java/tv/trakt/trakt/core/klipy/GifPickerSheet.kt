@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.klipy

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.core.klipy.model.Gif
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

/**
 * GIF picker: search input on top, trending GIFs below, search results in their place while the
 * user types. Tapping a GIF closes the sheet and reports the pick through [onGifSelected].
 */
@Composable
internal fun GifPickerSheet(
    visible: Boolean,
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    onGifSelected: (Gif) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // A fresh ViewModel per opening - the picker always starts on trending with an empty input.
    val viewModelKey = remember(visible) { nextInt().toString() }

    if (visible) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            GifPickerView(
                viewModel = koinViewModel(key = viewModelKey),
                onGifClick = { gif ->
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onGifSelected(gif) },
                        onDismiss = onDismiss,
                    )
                },
                modifier = Modifier.padding(top = 8.dp),
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
