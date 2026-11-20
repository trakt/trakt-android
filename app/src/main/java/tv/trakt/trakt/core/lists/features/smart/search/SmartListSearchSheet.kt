package tv.trakt.trakt.core.lists.features.smart.search

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SmartListSearchSheet(
    kind: SmartListSearchKind?,
    values: ImmutableList<String>,
    onValuesChange: (ImmutableList<String>) -> Unit,
    onDismiss: () -> Unit,
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
) {
    val viewModelKey = remember(kind) { nextInt().toString() }

    if (kind != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            val viewModel: SmartListSearchViewModel = koinViewModel(
                key = viewModelKey,
                parameters = { parametersOf(kind) },
            )
            val searchState by viewModel.state.collectAsStateWithLifecycle()

            SmartListSearchView(
                kind = kind,
                state = searchState,
                values = values,
                onQueryChange = viewModel::setQuery,
                onValuesChange = onValuesChange,
                modifier = Modifier
                    .fillMaxHeight(0.9F)
                    .padding(top = 8.dp),
            )
        }
    }
}
