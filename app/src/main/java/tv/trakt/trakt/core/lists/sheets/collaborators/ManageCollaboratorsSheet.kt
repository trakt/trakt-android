package tv.trakt.trakt.core.lists.sheets.collaborators

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
    if (active && list != null) {
        ManageCollaboratorsSheetContent(
            state = state,
            list = list,
            onListMoved = onListMoved,
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageCollaboratorsSheetContent(
    state: SheetState,
    list: CustomList,
    onListMoved: (list: CustomList, destination: PersonalListType) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(list) { nextInt().toString() }
    val viewModel = koinViewModel<ManageCollaboratorsViewModel>(
        key = viewModelKey,
        parameters = { parametersOf(list) },
    )

    val sheetScope = rememberCoroutineScope()
    val snackbar = LocalSnackbarState.current
    val resources = LocalResources.current

    var movedTo by remember(list) { mutableStateOf<PersonalListType?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ListMoved -> movedTo = if (movedTo == null) event.destination else null
                LimitReached -> sheetScope.launch {
                    snackbar.showSnackbar(resources.getString(R.string.action_toast_collaborator_limit_reached))
                }
                Failed -> sheetScope.launch {
                    snackbar.showSnackbar(resources.getString(R.string.error_text_unexpected_error_short))
                }
            }
        }
    }

    val viewState by viewModel.state.collectAsStateWithLifecycle()

    TraktBottomSheet(
        sheetState = state,
        onDismiss = {
            movedTo?.let { onListMoved(list, it) }
            onDismiss()
        },
    ) {
        ManageCollaboratorsView(
            state = viewState,
            onAddClick = viewModel::addCollaborator,
            onRemoveClick = viewModel::removeCollaborator,
        )
    }
}
