@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.info

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.people.model.PersonCreditsRole
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun ShowInfoSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    show: Show?,
    onPersonClick: (person: Person, role: PersonCreditsRole) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
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
                onPersonClick = { person, role ->
                    sheetScope.dismissWithAction(
                        sheet = state,
                        onDismiss = onDismiss,
                        action = { onPersonClick(person, role) },
                    )
                },
                modifier = Modifier
                    .padding(bottom = 24.dp),
            )
        }
    }
}

private fun CoroutineScope.dismissWithAction(
    sheet: SheetState,
    action: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    launch {
        sheet.hide()
    }.invokeOnCompletion {
        if (!sheet.isVisible) {
            action()
            onDismiss()
        }
    }
}
