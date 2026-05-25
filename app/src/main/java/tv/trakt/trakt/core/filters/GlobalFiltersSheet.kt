package tv.trakt.trakt.core.filters

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GlobalFiltersSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean,
    options: GlobalFiltersOptions = GlobalFiltersOptions.Default,
    onUpdate: (GlobalFilter) -> Unit = { _ -> },
    onDismiss: () -> Unit,
) {
    val sheetKey = remember(active) { nextInt().toString() }

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            GlobalFiltersView(
                viewModel = koinViewModel(
                    key = sheetKey,
                    parameters = { parametersOf(options) },
                ),
                onUpdate = onUpdate,
            )
        }
    }
}
