@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.recommended.whythis

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.recommended.RecommendedItem
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun WhyThisSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    item: RecommendedItem?,
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(item) { nextInt().toString() }

    if (item != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            val viewModel: WhyThisViewModel = koinViewModel(
                key = viewModelKey,
                parameters = { parametersOf(item) },
            )
            val viewState by viewModel.state.collectAsStateWithLifecycle()

            WhyThisView(
                title = item.title,
                state = viewState,
            )
        }
    }
}
