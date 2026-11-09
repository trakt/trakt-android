@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.progress

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun ShowProgressSheet(
    show: Show?,
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    onMarkWatchedClick: () -> Unit,
    onRemoveWatchedClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(show) {
        nextInt().toString()
    }

    if (show != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ShowProgressView(
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(show) },
                ),
                onMarkWatchedClick = onMarkWatchedClick,
                onRemoveWatchedClick = onRemoveWatchedClick,
            )
        }
    }
}
