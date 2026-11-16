package tv.trakt.trakt.core.lists.features.details.info

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

private const val SHEET_HEIGHT_FRACTION = 0.85F

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListInfoSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, PartiallyExpanded, Expanded),
    ),
    list: CustomList?,
    onPersonClick: (Person) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    val viewModelKey = remember(list) { nextInt().toString() }
    val sheetHeight = LocalWindowInfo.current.containerDpSize.height * SHEET_HEIGHT_FRACTION

    if (list != null) {
        TraktBottomSheet(
            sheetState = state,
            contentWindowInsets = { WindowInsets() },
            onDismiss = onDismiss,
        ) {
            ListInfoView(
                list = list,
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(list.ids.trakt) },
                ),
                onPersonClick = { person ->
                    sheetScope
                        .launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onPersonClick(person)
                            }
                        }
                },
                modifier = Modifier
                    .height(sheetHeight)
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
            )
        }
    }
}
