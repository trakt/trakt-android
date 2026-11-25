package tv.trakt.trakt.core.lists.sheets.collaborators

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LeaveCollaborationConfirmation(
    list: CustomList?,
    onDismiss: () -> Unit,
    viewModel: LeaveCollaborationViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbarState.current
    val resources = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.failed.collect {
            scope.launch {
                snackbar.showSnackbar(resources.getString(R.string.error_text_unexpected_error_short))
            }
        }
    }

    ConfirmationSheet(
        active = list != null,
        title = stringResource(R.string.confirmation_title_leave_collaboration),
        message = stringResource(R.string.warning_prompt_leave_collaboration, list?.name.orEmpty()),
        yesText = stringResource(R.string.button_text_leave_collaboration),
        yesColor = Red500,
        onYes = {
            list?.let(viewModel::leave)
            onDismiss()
        },
        onNo = onDismiss,
    )
}
