@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.info

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
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun ShowInfoSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    show: Show?,
    onPersonClick: (person: Person) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(show) { nextInt().toString() }

    if (show != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            ShowInfoView(
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(show) },
                ),
                onPersonClick = onPersonClick,
                modifier = Modifier
                    .padding(bottom = 24.dp),
            )
        }
    }
}
