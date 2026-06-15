package tv.trakt.trakt.helpers.editscreen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditScreenSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    ),
    active: Boolean,
    enabledValues: Set<EditScreenKey>,
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(active, enabledValues) {
        nextInt().toString()
    }

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = {
                onDismiss()
            },
        ) {
            EditScreenView(
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(enabledValues) },
                ),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
