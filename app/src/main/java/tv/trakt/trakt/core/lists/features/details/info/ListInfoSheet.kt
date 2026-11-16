package tv.trakt.trakt.core.lists.features.details.info

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

private const val SHEET_MAX_HEIGHT_FRACTION = 0.85F

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListInfoSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    list: CustomList?,
    onPersonClick: (Person) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(list) { nextInt().toString() }
    val maxHeight = LocalWindowInfo.current.containerDpSize.height * SHEET_MAX_HEIGHT_FRACTION

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
                onPersonClick = onPersonClick,
                modifier = Modifier
                    .heightIn(max = maxHeight)
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
            )
        }
    }
}
