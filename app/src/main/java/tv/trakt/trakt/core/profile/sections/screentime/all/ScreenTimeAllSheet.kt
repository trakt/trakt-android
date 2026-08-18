@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.screentime.all

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun ScreenTimeAllSheet(
    state: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    data: ScreenTimeData,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(visible, data) {
        nextInt().toString()
    }

    if (visible) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ScreenTimeAllView(
                viewModel = koinViewModel(key = viewModelKey) { parametersOf(data) },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 20.dp),
            )
        }
    }
}
