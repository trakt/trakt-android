package tv.trakt.trakt.core.lists.sheets.collaborators

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sheets.collaborators.ManageCollaboratorsEvent.Failed
import tv.trakt.trakt.core.lists.sheets.collaborators.ManageCollaboratorsEvent.LimitReached
import tv.trakt.trakt.core.lists.sheets.collaborators.ManageCollaboratorsEvent.ListMoved
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageCollaboratorsSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    active: Boolean,
    list: CustomList?,
    onListMoved: (list: CustomList, destination: PersonalListType) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()
    val snackbar = LocalSnackbarState.current
    val resources = LocalResources.current

    val viewModelKey = remember(list) { nextInt().toString() }
    var movedTo by remember(list) { mutableStateOf<PersonalListType?>(null) }

    if (active && list != null) {
        TraktBottomSheet(
            sheetState = state,
            contentWindowInsets = { WindowInsets() },
            onDismiss = {
                movedTo?.let { onListMoved(list, it) }
                onDismiss()
            },
        ) {
            ManageCollaboratorsView(
                viewModel = koinViewModel(
                    key = viewModelKey,
                    parameters = { parametersOf(list) },
                ),
                modifier = Modifier.fillMaxHeight(0.9F),
                onListMoved = { destination ->
                    movedTo = if (movedTo == null) destination else null
                },
                onLimitReached = {
                    sheetScope.launch {
                        snackbar.showSnackbar(resources.getString(R.string.action_toast_collaborator_limit_reached))
                    }
                },
                onError = {
                    sheetScope.launch {
                        snackbar.showSnackbar(resources.getString(R.string.error_text_unexpected_error_short))
                    }
                },
            )
        }
    }
}
