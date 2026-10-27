@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.filters

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

private const val SHEET_MAX_HEIGHT_FRACTION = 0.8F

@Composable
internal fun GlobalFiltersSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    active: Boolean,
    options: GlobalFiltersOptions = GlobalFiltersOptions.Default,
    onUpdate: (GlobalFilter) -> Unit = { _ -> },
    onDismiss: () -> Unit,
) {
    val sheetKey = remember(active) { nextInt().toString() }
    val maxHeight = LocalWindowInfo.current.containerDpSize.height * SHEET_MAX_HEIGHT_FRACTION

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            Box(
                modifier = Modifier.heightIn(max = maxHeight),
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
}
