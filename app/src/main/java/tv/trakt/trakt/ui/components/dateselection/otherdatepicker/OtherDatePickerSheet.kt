@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.dateselection.otherdatepicker

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.ui.components.TraktBottomSheet
import java.time.Instant
import kotlin.random.Random.Default.nextInt

@Composable
internal fun OtherDatePickerSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    onResult: (date: Instant) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(visible) {
        nextInt().toString()
    }

    if (visible) {
        TraktBottomSheet(
            sheetState = state,
            contentWindowInsets = { WindowInsets() },
            onDismiss = onDismiss,
        ) {
            OtherDatePickerView(
                viewModel = koinViewModel(key = viewModelKey),
                title = title,
                subtitle = subtitle,
                onConfirm = onResult,
                contentPadding = PaddingValues(horizontal = 24.dp),
                modifier = Modifier
                    .fillMaxHeight(0.92F),
            )
        }
    }
}
